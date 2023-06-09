package com.example.angry_alarm

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.angry_alarm.databinding.FragmentMassageBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.angry_alarm.textdb.TextDaoDatabase
import com.example.angry_alarm.textdb.TextTable

class MessageFragment : Fragment() {
    private var _binding: FragmentMassageBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TextAdapter
    private lateinit var db: TextDaoDatabase
    lateinit var dbHelper: AlarmDatabase.MyDbHelper

    private var alarmId: Int? = 0
    private var alarm: MyElement? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMassageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = TextDaoDatabase.getDatabase(requireContext())!!
        adapter = TextAdapter(mutableListOf())

        alarmId = arguments?.getInt("alarmId")
        setupRecyclerView()
        alarmId?.let { loadMessagesFromDatabaseById(it) }?: run {
            loadMessageFromDatabaseAll()
        }
        alarmId?.let { setupButtonClickListeners(it) }
        Log.d("AlarmID","$alarmId")
        // 아마 다시 알림 쪽이랑 연동하면서 fullscreenActivity랑 연동해서 사용해야 할 듯.
        alarmId?.let { sendMessage(it) }
    }
    // recyclerview 설정
    private fun setupRecyclerView() {
        binding.messageView.layoutManager = LinearLayoutManager(requireContext())
        binding.messageView.addItemDecoration(
            DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        )
        binding.messageView.adapter = adapter

        adapter.setItemClickListener(object : TextAdapter.OnItemClickListener {
            override fun onClick(v: View, position: Int) {
                CoroutineScope(Dispatchers.Main).launch {
                    val messageCopy = CoroutineScope(Dispatchers.IO).async {
                        db!!.textDAO().selectByMessage(adapter.getElement(position).message)
                    }.await()
                    Toast.makeText(requireContext(), "문구가 복사 되었습니다.", Toast.LENGTH_SHORT).show()
                    binding.message.setText(messageCopy.message)

                    val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText("message", messageCopy.message)
                    clipboardManager.setPrimaryClip(clipData)
                }
            }
        })

        adapter.setOnItemLongClickListener(object : TextAdapter.OnItemLongClickEventListener {
            override fun onItemLongClick(v: View, position: Int) {
                CoroutineScope(Dispatchers.Main).launch {
                    val newList = CoroutineScope(Dispatchers.IO).async {
                        db.textDAO().deleteByMessage(adapter.getElement(position).message.toString())
                        db.textDAO().selectAll()
                    }.await()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "${adapter.getElement(position).message} 문구 삭제.",
                            Toast.LENGTH_SHORT
                        ).show()
                        adapter.setList(newList)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        })
    }
    //저장하기 & 데이터베이스 연동
    private fun setupButtonClickListeners(alarmId: Int) {
        binding.save.setOnClickListener {
            val elem = TextTable(
                alarmId,
                binding.message.text.toString(),
                binding.telNum.text.toString()
            )
            Log.d("setupButtonClickListeners","$alarmId")
            CoroutineScope(Dispatchers.IO).launch {
                db!!.textDAO().insert(elem)
                val newList = CoroutineScope(Dispatchers.IO).async {
                    db!!.textDAO().selectByAlarmId(alarmId)
                }.await()
                withContext(Dispatchers.Main) {
                    adapter.setList(newList)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }
    //데이터 베이스에 있는 메시지 보여주기용.
    private fun loadMessagesFromDatabaseById(alarmId: Int) {
        db = TextDaoDatabase.getDatabase(requireContext())!!
        CoroutineScope(Dispatchers.Main).launch {
            val getList = CoroutineScope(Dispatchers.IO).async {
                db.textDAO().selectByAlarmId(alarmId)
            }.await()
            withContext(Dispatchers.Main) {
                adapter.setList(getList)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun loadMessageFromDatabaseAll(){
        db = TextDaoDatabase.getDatabase(requireContext())!!
        CoroutineScope(Dispatchers.Main).launch {
            val getList = CoroutineScope(Dispatchers.IO).async {
                db.textDAO().selectAll()
            }.await()
            withContext(Dispatchers.Main) {
                adapter.setList(getList)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun sendMessage(alarmId: Int){
        dbHelper = AlarmDatabase.MyDbHelper(requireContext())
        val alarmDB = dbHelper.readableDatabase
        val myEntry = AlarmDatabase.MyDBContract.MyEntry
        val selection = "${myEntry.alarm_id} = ?"
        val selectionArgs = arrayOf(alarmId.toString())
        val cursor = alarmDB.query(myEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null)

        cursor.moveToFirst()
        val id = cursor.getInt(cursor.getColumnIndexOrThrow(myEntry.alarm_id))
        val title = cursor.getString(cursor.getColumnIndexOrThrow(myEntry.title))
        val hour = cursor.getInt(cursor.getColumnIndexOrThrow(myEntry.hour))
        val minute = cursor.getInt(cursor.getColumnIndexOrThrow(myEntry.minute))
        val alarmDays = cursor.getString(cursor.getColumnIndexOrThrow(myEntry.alarm_days))
        //val repeatCount = cursor.getInt(cursor.getColumnIndexOrThrow(myEntry.repeat_count))
        //val repeatInterval = cursor.getLong(cursor.getColumnIndexOrThrow(myEntry.repeat_interval))
        val isVibrator = cursor.getInt(cursor.getColumnIndexOrThrow(myEntry.isVibrator)) == 1
        val isSwitchOn = cursor.getInt(cursor.getColumnIndexOrThrow(myEntry.isSwitchOn)) == 1

        cursor.close()
        alarmDB.close()

        alarm = MyElement(id, title, hour, minute, alarmDays, isVibrator, isSwitchOn)

        db = TextDaoDatabase.getDatabase(requireContext())!!
        CoroutineScope(Dispatchers.Main).launch {
            var getList =
                withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                    db.textDAO().selectByAlarmId(alarmId)
                }
            withContext(Dispatchers.Main) {
                for (element in getList){
                    val pNumber = element.phoneNumber
                    val sms = element.message
                    try {
                        val smsManager: SmsManager = SmsManager.getDefault()
                        smsManager.sendTextMessage(pNumber, null, sms, null, null)
                        Log.d("전송 완료","$sms")
                    } catch (e: Exception) {
                        Log.d("실패","$e")
                        e.printStackTrace()
                    }
                }
            }//withContext 종료
        }//CoroutineScope
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
