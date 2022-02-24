package com.androiddevs.shoppinglisttestingyt.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androiddevs.shoppinglisttestingyt.data.local.ShoppingItem
import com.androiddevs.shoppinglisttestingyt.data.remote.responses.ImageResponse
import com.androiddevs.shoppinglisttestingyt.data.repository.ShoppingRepository
import com.androiddevs.shoppinglisttestingyt.other.Constants
import com.androiddevs.shoppinglisttestingyt.other.Event
import com.androiddevs.shoppinglisttestingyt.other.Resource
import kotlinx.coroutines.launch
import java.lang.Exception

class ShoppingViewModel @ViewModelInject constructor(
    private val repository: ShoppingRepository
): ViewModel() {

    val shoppingItems = repository.observeAllShoppingItems()

    val totalPrice = repository.observeTotalPrice()

    private val _images = MutableLiveData<Event<Resource<ImageResponse>>>()
    val images : LiveData<Event<Resource<ImageResponse>>> = _images

    private val _curImageUrl = MutableLiveData<String>()
    val curImageUrl : LiveData<String> = _curImageUrl

    private val _insertShoppingItemStatus = MutableLiveData<Event<Resource<ShoppingItem>>>()
    val insertShoppingItemStatus : LiveData<Event<Resource<ShoppingItem>>> = _insertShoppingItemStatus

    fun setCurImageUrl(url : String){
        _curImageUrl.postValue(url)
    }

    fun deleteShoppingItem(shoppingItem: ShoppingItem) = viewModelScope.launch {
        repository.deleteShoppingItem(shoppingItem)
    }

    fun insertShoppingItemIntoDb(shoppingItem: ShoppingItem) = viewModelScope.launch {
        repository.insertShoppingItem(shoppingItem)
    }

    fun insertShoppingItem(name : String, amountString : String, priceString : String){
        if(name.isEmpty() || amountString.isEmpty() || priceString.isEmpty()){
            _insertShoppingItemStatus.postValue(Event(Resource.error("the fields must not be empty",null)))
            return
        }
        if(name.length > Constants.MAX_NAME_LENGTH){
            _insertShoppingItemStatus.postValue(Event(Resource.error("the name of the item" +
                    "must not exceed ${Constants.MAX_NAME_LENGTH} characters",null)))
            return
        }
        if(priceString.length > Constants.MAX_PRICE_LENGTH){
            _insertShoppingItemStatus.postValue(Event(Resource.error("the price of the item" +
                    "must not exceed ${Constants.MAX_PRICE_LENGTH} characters",null)))
            return
        }
        val amount = try{
            amountString.toInt()
        } catch(e:Exception){
            _insertShoppingItemStatus.postValue(Event(Resource.error("please enter a valid amount",null)))
            return
        }

        val shoppingItem = ShoppingItem(name,amount,priceString.toFloat(),_curImageUrl.value?:"")
        insertShoppingItemIntoDb(shoppingItem)
        setCurImageUrl("")
        _insertShoppingItemStatus.postValue(Event(Resource.success(shoppingItem)))

    }

    fun searchForImage(imageQuery : String){
        if(imageQuery.isEmpty()){
            return
        }
        //LiveData.value를 하는이유 (postvalue가 아니라)
        //value를 사용하면 바로 값을 변경, postValue는 만약 바로 다음에 또 값을 변경하면 최근에 변경된값만 Return(최적화)
        //그래서 loading 후 success를 바로 호출하기 때문에 loading을 할당해주기 위해서 value를 사용
        //그래야 test할 때 loading을 test해볼 수 있다. 아니면 바로 success 값을 Return해주기 때문
        //실제 앱에서는 postValue사용해도 상관없음
        _images.value = Event(Resource.loading(null))

        viewModelScope.launch {
            val response = repository.searchForImage(imageQuery)
            _images.value = Event(response)//response 변수는 resource타입으로 반환됨
        }

    }

}