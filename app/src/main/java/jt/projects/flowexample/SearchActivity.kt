package jt.projects.flowexample

import android.os.Bundle
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class SearchActivity : AppCompatActivity() {

    private val job = Job()
    private val queryStateFlow = MutableStateFlow("")
    private lateinit var searchView: SearchView
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        searchView = findViewById(R.id.search_view)
        textView = findViewById(R.id.result_text_view)

        setUpSearchFlow()

    }

    private fun setUpSearchFlow() {
        CoroutineScope(Dispatchers.Main + job).launch {
            searchView.getQueryTextChangeStateFlow().debounce(500)
                .filter { query ->     //фильтрует пустые строки.
                    if (query.isEmpty()) {
                        textView.text = ""
                        return@filter false
                    } else {
                        return@filter true
                    }
                }
                .distinctUntilChanged()       //позволяет избегать дублирующие запросы
                .flatMapLatest { query ->// возвращает в поток только самый последний запрос и игнорирует более ранние
                    dataFromNetwork(query)
                        .catch {
                            emit("")
                        }
                }
                .collect { result -> textView.text = result }
        }

        searchView.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { queryStateFlow.value = it }
                return true
            }
            override fun onQueryTextChange(newText: String): Boolean {
                queryStateFlow.value = newText
                return true
            }
        })
    }

    fun SearchView.getQueryTextChangeStateFlow(): StateFlow<String> {
        val query = MutableStateFlow("")
        setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { queryStateFlow.value = it }
                return true
            }
            override fun onQueryTextChange(newText: String): Boolean {
                query.value = newText
                return true
            }
        })
        return query
    }



    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    //Имитируем загрузку данных по результатам ввода
    private fun dataFromNetwork(query: String): Flow<String> {
        return flow {
            delay(2000)
            emit(query)
        }
    }
}