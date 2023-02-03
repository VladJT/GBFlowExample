package jt.projects.randomint

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

internal data class Data(
    val data: String
)

internal object DataBase {
    fun fetchData() = (0..1000).random()
}

// Класс DataSource принимает в конструктор два аргумента: базу
//данных и период обновления данных. Период равен одной секунде, указанной в миллисекундах. Он
//содержит одну переменную типа Flow<String>, в которой находятся из нашей БД, переведенные из Int
//в String. Чтобы создать простой поток нужно воспользоваться flow builder. В нашем случае это простая
//функция flow, в которой все и происходит
internal class DataSource(
    private val database: DataBase = DataBase,
    private val refreshIntervalMs: Long = 1000
) {
    val data: Flow<String> = flow {
        while (true) {
            val num = database.fetchData()
            emit(num.toString())
            delay(refreshIntervalMs)
        }
    }
        .flowOn(Dispatchers.Default)
        .catch { e ->
            println("$e")
        }

}


internal class Repository(
    dataSource: DataSource = DataSource()
){
    val userData: Flow<Data> = dataSource
        .data
        .map {  Data(it) }
    //.onEach { saveInCache(it) }//Оператор onEach опционален. Он показывает, что значение, возвращаемое DataSource можно
    //сохранить для дальнейшего использования или совершить с ним неограниченное количество
    //операций
}

internal class MainViewModel(
    repository: Repository = Repository()
): ViewModel(){
    val liveData : LiveData<Data> = repository.userData.asLiveData()
//
//    init{
//        viewModelScope.launch {
//            repository.userData.flowOn(Dispatchers.Main).collect{
//                liveData.value = it
//            }
//        }
//    }

}