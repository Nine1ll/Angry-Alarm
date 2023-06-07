package com.example.angryalarm

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.angryalarm.databinding.FragmentAlarmBinding

class AlarmViewFragment : Fragment() {
    private lateinit var binding: FragmentAlarmBinding
    private lateinit var dbHelper: AlarmDatabase.MyDbHelper
    private lateinit var adapter: AlarmAdaptor
    private lateinit var mainActivity: MainActivity
    private lateinit var comm: Communicator
    private var alarmId: Int? = 0
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

        adapter.setOnItemClickListener { position: Int ->
            val alarmId = adapter.getItemSelectionKey(position)
            alarmId?.let { mainActivity.onAlarmSelected(it) }
            comm = requireActivity() as Communicator
            if (alarmId != null) {
                comm.passDataCom(alarmId)
            }
        }
    }

}
