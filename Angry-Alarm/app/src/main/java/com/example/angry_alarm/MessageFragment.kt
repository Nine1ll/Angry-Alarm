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
import androidx.room.util.CursorUtil.getColumnIndexOrThrow
import com.example.angry_alarm.AlarmDatabase.MyDBContract.MyEntry.isVibrator
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

    private var alarmId: Int? = 0

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
//        alarmId?.let { sendMessage(it) }
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

            if (binding.message.text.isEmpty()) {
                Toast.makeText(requireContext(), "메시지를 입력해주세오.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (binding.telNum.text.isEmpty()) {
                Toast.makeText(requireContext(), "번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
