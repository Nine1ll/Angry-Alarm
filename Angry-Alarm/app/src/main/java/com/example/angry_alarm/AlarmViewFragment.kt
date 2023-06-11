package com.example.angry_alarm

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.angry_alarm.databinding.FragmentAlarmBinding
import com.example.angry_alarm.textdb.TextDAO
import com.example.angry_alarm.textdb.TextDaoDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmViewFragment : Fragment() {
    private lateinit var binding: FragmentAlarmBinding
    private lateinit var dbHelper: AlarmDatabase.MyDbHelper
    private lateinit var adapter: AlarmAdaptor
    private lateinit var mainActivity: MainActivity
    private lateinit var comm: Communicator
    private lateinit var db : TextDaoDatabase
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MainActivity) {
            mainActivity = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAlarmBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = AlarmDatabase.MyDbHelper(requireContext())

        val getList = dbHelper.selectAll()

        adapter = AlarmAdaptor(getList)

        binding.alarmsList.layoutManager = LinearLayoutManager(requireContext())
        binding.alarmsList.adapter = adapter
        binding.alarmsList.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))

        adapter.setOnCheckedChangeListener { alarmId: Int, isChecked: Boolean ->
            dbHelper.updateSwitchStatus(alarmId, isChecked)

            val updatedList = dbHelper.selectAll()
            adapter.setList(updatedList)
            adapter.notifyDataSetChanged()

            // 알람이 켜졌음을 나타내는 Toast 메시지 표시
            if (isChecked) {
                Toast.makeText(requireContext(), "알람이 켜졌습니다.", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(requireContext(), "알람이 꺼졌습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        adapter.setOnItemClickListener { position: Int ->
            val alarmId = adapter.getItemSelectionKey(position)
            alarmId?.let { mainActivity.onAlarmSelected(it)}
            comm = requireActivity() as Communicator
            if (alarmId != null) {
                comm.passDataCom(alarmId)
            }
        }


        adapter.setOnLongClickListener { position: Int ->
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("알람 설정")

                .setPositiveButton("삭제") { dialog, id ->
                    dialog.dismiss()
                    val confirmDialog = AlertDialog.Builder(requireContext())
                    confirmDialog.setTitle("알람 삭제")
                        .setMessage("선택한 알람을 삭제하시겠습니까?")
                        .setPositiveButton("확인") { _, _ ->
                            val alarmId: Int? = adapter.getItemSelectionKey(position)
                            val mainActivity = requireActivity() as MainActivity
                            if (alarmId != null) {
                                mainActivity.cancel(alarmId)
                            }
                            dbHelper.delEntry(alarmId!!)
                            val getList = dbHelper.selectAll()
                            adapter.setList(getList)
                        }
                        .setNegativeButton("취소") { _, _ ->
                            // 취소 버튼 클릭 시 처리 로직
                        }
                        .show()
                }
                .setNegativeButton("수정") { dialog, id ->
                    // 수정 버튼 클릭 시 처리 로직
                    val alarmId: Int? = adapter.getItemSelectionKey(position)
                    alarmId?.let { mainActivity.onAlarmSelected(it)}
                }
                .show()
        }
    }
}
