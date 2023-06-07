package kr.nine1ll.newtext

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.nine1ll.newtext.databinding.ActivityMainBinding
import kr.nine1ll.newtext.textdb.TextDaoDatabase
import kr.nine1ll.newtext.textdb.TextTable

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        var db = TextDaoDatabase.getDatabase(applicationContext)

        var tempEntry = mutableListOf(
            TextTable("일어나기 힘들어요. 깨워주세요.","01012341234"),
            TextTable("문자 받으면 전화 바랍니다.","01012341234"),
            TextTable("안녕하세요! 깨워주실 수 있을까요? 오늘 하루도 함께 시작해봐요.","01012341234")
        )
        for (entry in tempEntry){
            CoroutineScope(Dispatchers.IO).launch {
                db!!.textDAO().insert(entry)
            }
        }
        val adapter = TextAdapter(mutableListOf())

        CoroutineScope(Dispatchers.Main).launch {
            val getList = CoroutineScope(Dispatchers.IO).async {
                db!!.textDAO().selectAll()
            }.await()
            withContext(Dispatchers.Main){
                adapter.setList(getList)
                binding.messageView.adapter = adapter
            }
        }

        binding.messageView.layoutManager = LinearLayoutManager(this)
        binding.messageView.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))

        adapter.setItemClickListener(object:TextAdapter.OnItemClickListener{
            override fun onClick(v: View, position: Int) {
                CoroutineScope(Dispatchers.Main).launch {
                    val messageCopy = CoroutineScope(Dispatchers.IO).async {
                        db!!.textDAO().selectByMessage(adapter.getElement(position).message)
                    }.await()
                    Toast.makeText(applicationContext,"문구가 복사 되었습니다.",
                        Toast.LENGTH_SHORT).show()
                    binding.message.setText(messageCopy.message)

                    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText("message", messageCopy.message)
                    clipboardManager.setPrimaryClip(clipData)
                }
            }
        })

        adapter.setOnItemLongClickListener(object:TextAdapter.OnItemLongClickEventListener{
            override fun onItemLongClick(v: View, position: Int) {
                CoroutineScope(Dispatchers.Main).launch {
                    val newList =  CoroutineScope(Dispatchers.IO).async {
                        db!!.textDAO().deleteByMessage(adapter.getElement(position).message)
                        db!!.textDAO().selectAll()
                    }.await()
                    withContext(Dispatchers.Main){
                        Toast.makeText(applicationContext,"${adapter.getElement(position).message} is deleted",
                            Toast.LENGTH_SHORT).show()
                        adapter.setList(newList)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        })

        binding.save.setOnClickListener {
            val elem = TextTable(
                binding.message.text.toString(),
                binding.telNum.text.toString()
            )
            CoroutineScope(Dispatchers.IO).launch {
                db!!.textDAO().insert(elem)
                val newList = CoroutineScope(Dispatchers.IO).async {
                    db!!.textDAO().selectAll()
                }.await()
                withContext(Dispatchers.Main){
                    adapter.setList(newList)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }
}