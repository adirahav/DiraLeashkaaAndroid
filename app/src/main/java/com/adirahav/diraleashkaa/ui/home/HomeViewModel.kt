package com.adirahav.diraleashkaa.ui.home

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.adirahav.diraleashkaa.common.Configuration
import com.adirahav.diraleashkaa.common.Enums
import com.adirahav.diraleashkaa.common.Utilities
import com.adirahav.diraleashkaa.data.network.DatabaseClient
import com.adirahav.diraleashkaa.data.network.entities.BestYieldEntity
import com.adirahav.diraleashkaa.data.network.entities.FixedParametersEntity
import com.adirahav.diraleashkaa.data.network.entities.PropertyEntity
import com.adirahav.diraleashkaa.data.network.models.*
import com.adirahav.diraleashkaa.data.network.requests.PropertyArchiveRequest
import com.adirahav.diraleashkaa.data.network.services.PropertyService
import com.adirahav.diraleashkaa.data.network.services.UserService
import com.adirahav.diraleashkaa.ui.base.BaseViewModel
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class HomeViewModel internal constructor(
        private val activity: HomeActivity,
        private val propertyService: PropertyService,
        private val userService: UserService

        ) : BaseViewModel() {

    private val TAG = "HomeViewModel"

    // fixed parameters
    val localFixedParametersCallback: MutableLiveData<FixedParametersEntity> = MutableLiveData()

    // my cities
    val localMyCitiesCallback: MutableLiveData<List<PropertyEntity>> = MutableLiveData()

    // city's properties
    val localCityPropertiesCallback: MutableLiveData<ArrayList<PropertyEntity>>

    // delete property
    val localDeleteProperty: MutableLiveData<Int>
    val serverDeleteProperty: MutableLiveData<Int>

    // best yield
    val localBestYieldCallback: MutableLiveData<List<BestYieldEntity>>

    // home
    val homeCallback: MutableLiveData<HomeModel>

    init {
        // fixed parameters

        // my cities

        // city's properties
        localCityPropertiesCallback = MutableLiveData()

        // delete property
        localDeleteProperty = MutableLiveData()
        serverDeleteProperty = MutableLiveData()

        // best yield
        localBestYieldCallback = MutableLiveData()

        // home
        homeCallback = MutableLiveData()
    }

    //region fixed parameters
    fun getLocalFixedParameters(applicationContext: Context) {
        Utilities.log(Enums.LogType.Debug, TAG, "getLocalFixedParameters()")

        CoroutineScope(Dispatchers.IO).launch {
            val fixedParameters = DatabaseClient.getInstance(applicationContext)?.appDatabase?.fixedParametersDao()?.getAll()
            Timer("FixedParameters", false).schedule(Configuration.LOCAL_AWAIT_MILLISEC) {
                setLocalFixedParameters(fixedParameters?.first())
            }
        }
    }

    private fun setLocalFixedParameters(fixedParameters: FixedParametersEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "localFixedParametersCallback()", showToast = false)
        this.localFixedParametersCallback.postValue(fixedParameters)
    }
    //endregion fixed parameters

    //region my cities
    fun getLocalMyCities(applicationContext: Context) {
        Utilities.log(Enums.LogType.Debug, TAG, "getLocalMyCities()")
        CoroutineScope(Dispatchers.IO).launch {
            //getMyCitiesSuspend(applicationContext, lifecycleOwner)
            val resultsProperties = DatabaseClient.getInstance(applicationContext)?.appDatabase?.propertyDao()?.getMyCities()
            setMyCities(resultsProperties)
        }
    }

    private fun setMyCities(cities: List<PropertyEntity>?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setMyCities()", showToast = false)
        this.localMyCitiesCallback.postValue(cities)
    }

    //endregion my cities

    //region city's properties
    fun getCityProperties(applicationContext: Context, city: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            val resultProperties = DatabaseClient.getInstance(applicationContext)?.appDatabase?.propertyDao()?.getCityProperties(city)
            setCityProperties(resultProperties as ArrayList<PropertyEntity>)
        }
    }

    private fun setCityProperties(properties: ArrayList<PropertyEntity>?) {
        this.localCityPropertiesCallback.postValue(properties)
    }

    //endregion city's properties

    //region delete property
    // == SERER =====
    fun deleteServerProperty(applicationContext: Context, propertyData: PropertyEntity?) {
        Utilities.log(Enums.LogType.Debug, TAG, "deleteServerProperty()")

        CoroutineScope(Dispatchers.IO).launch {

            /**/
            val call: Call<HomeModel?>? = propertyService.propertyAPI.deleteFromHome(
                    "Bearer ${activity.userToken}",
                    PropertyArchiveRequest(_id = propertyData!!._id!!, dataToReturn = "home"),
                    propertyData._id!!)
            call?.enqueue(object : Callback<HomeModel?> {
                override fun onResponse(call: Call<HomeModel?>, response: Response<HomeModel?>) {

                    Utilities.log(Enums.LogType.Debug, TAG, "deleteServerProperty(): response = $response")
                    val result: HomeModel? = response.body()

                    if (response.code() == 200) {
                        try {
                            Utilities.log(Enums.LogType.Debug, TAG, "deleteServerProperty(): response = $response")
                            saveLocalHome(applicationContext, result)
                        }
                        catch (e: Exception) {
                            Utilities.log(Enums.LogType.Error, TAG, "deleteServerProperty(): e = ${e.message} ; result.data = ${response.message()}")
                            setHome(null)
                        }
                    }
                    else {
                        Utilities.log(Enums.LogType.Error, TAG, "deleteServerProperty(): response = $response")
                        setHome(null)
                    }
                }

                override fun onFailure(call: Call<HomeModel?>, t: Throwable) {
                    Utilities.log(Enums.LogType.Error, TAG, "deleteServerProperty(): onFailure = $t")
                    setHome(null)
                    call.cancel()
                }
            })
            /**/

            /*

            call?.enqueue(object : Callback<PropertyModel?> {
                override fun onResponse(call: Call<PropertyModel?>, response: Response<PropertyModel?>) {
                    Utilities.log(Enums.LogType.Debug, TAG, "deleteServerProperty(): response = $response")
                    val result: PropertyModel? = response.body()

                    if (response.code() == 200) {
                        //setPropertiesAfterServerDelete(result)
                    }
                    else {
                        setPropertiesAfterServerDelete(0)
                        Utilities.log(Enums.LogType.Error, TAG, "deleteServerProperty(): Error = $response ; errorCode = ${result?.error?.errorCode} ; errorMessage = ${result?.error?.errorMessage}", propertyData = propertyData)
                    }
                }

                override fun onFailure(call: Call<PropertyModel?>, t: Throwable) {
                    setPropertiesAfterServerDelete(0)
                    Utilities.log(Enums.LogType.Error, TAG, "deleteServerProperty(): onFailure = $t")
                    call.cancel()
                }
            })*/
        }
    }

    private fun setPropertiesAfterServerDelete(deletedItemsCount: Int) {
        Utilities.log(Enums.LogType.Debug, TAG, "setPropertiesAfterServerDelete()", showToast = false)
        this.serverDeleteProperty.postValue(deletedItemsCount)
    }


    //endregion delete property

    //region best yield
    fun getLocalBestYield(applicationContext: Context) {
        Utilities.log(Enums.LogType.Debug, TAG, "getLocalBestYield()")

        CoroutineScope(Dispatchers.IO).launch {
            val resultProperties = DatabaseClient.getInstance(applicationContext)?.appDatabase?.bestYieldDao()?.getAll()
            setLocalBestYield(resultProperties)
        }
    }

    private fun setLocalBestYield(bestYield: List<BestYieldEntity>?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setLocalBestYield()", showToast = false)
        this.localBestYieldCallback.postValue(bestYield)
    }
    //endregion best yield

    //region home
    fun getServerHome(applicationContext: Context) {
        Utilities.log(Enums.LogType.Debug, TAG, "getServerHome()")

        CoroutineScope(Dispatchers.IO).launch {

            val call: Call<HomeModel?>? = userService.userAPI.getHomeData("Bearer ${activity.userToken}")
            call?.enqueue(object : Callback<HomeModel?> {
                override fun onResponse(call: Call<HomeModel?>, response: Response<HomeModel?>) {

                    Utilities.log(Enums.LogType.Debug, TAG, "getServerHome(): response = $response")

                    val result: HomeModel? = response.body()

                    if (response.code() == 200) {
                        try {
                            Utilities.log(Enums.LogType.Debug, TAG, "getServerHome(): response = $response")
                            saveLocalHome(applicationContext, result)
                        }
                        catch (e: Exception) {
                            Utilities.log(Enums.LogType.Error, TAG, "getServerHome(): e = ${e.message} ; result.data = ${response.message()}")
                            setHome(null)
                        }
                    }
                    else {
                        Utilities.log(Enums.LogType.Error, TAG, "getServerHome(): response = $response")
                        setHome(null)
                    }
                }

                override fun onFailure(call: Call<HomeModel?>, t: Throwable) {
                    Utilities.log(Enums.LogType.Error, TAG, "getServerHome(): onFailure = $t")
                    setHome(null)
                    call.cancel()
                }
            })
        }
    }

    private fun setHome(home: HomeModel?) {
        Utilities.log(Enums.LogType.Debug, TAG, "setHome()", showToast = false)
        this.homeCallback.postValue(home)
    }

    fun saveLocalHome(applicationContext: Context, home: HomeModel?) {
        Utilities.log(Enums.LogType.Debug, TAG, "saveLocalHome(): home = ${home.toString()}")
        CoroutineScope(Dispatchers.IO).launch {

            // ----------------
            // properties
            // ----------------
            val localPropertiesList = DatabaseClient.getInstance(applicationContext)?.appDatabase?.propertyDao()?.getAll()

            var oldProperties = if (localPropertiesList != null)
                                    ArrayList(localPropertiesList.map { it.copy() }).filter { item -> item._id != "" }
                                else
                                    null

            if (oldProperties != null) {
                for (property in oldProperties) {
                    property.roomID = null
                }
            }

            if (localPropertiesList != null) {
                if (localPropertiesList.isNotEmpty()) {
                    DatabaseClient.getInstance(applicationContext)?.appDatabase?.propertyDao()?.deleteAll()!!
                }
            }

            val newProperties = home?.properties
            if (newProperties != null) {
                for (newProperty in newProperties) {
                    DatabaseClient.getInstance(applicationContext)?.appDatabase?.propertyDao()?.insert(newProperty)!!
                }
            }

            val emptyProperty = home?.emptyProperty
            if (emptyProperty != null) {
                emptyProperty.roomID = 0L
                DatabaseClient.getInstance(applicationContext)?.appDatabase?.propertyDao()?.insert(emptyProperty)!!
            }

            val isPropertiesNeedToRefresh = oldProperties?.equals(newProperties) != true

            // ----------------
            // best yield
            // ----------------
            val localBestYieldList = DatabaseClient.getInstance(applicationContext)?.appDatabase?.bestYieldDao()?.getAll()

            if (localBestYieldList != null && localBestYieldList.isNotEmpty()) {
                DatabaseClient.getInstance(applicationContext)?.appDatabase?.bestYieldDao()?.deleteAll()!!
            }

            val oldBestYieldList = if (localBestYieldList != null)
                ArrayList(localBestYieldList.map { it.copy() })
            else
                null

            if (oldBestYieldList != null) {
                for (property in oldBestYieldList) {
                    property.roomID = null
                }
            }

            val newBestYields = home?.bestYields
            if (newBestYields != null) {
                for (newBestYield in newBestYields) {
                    DatabaseClient.getInstance(applicationContext)?.appDatabase?.bestYieldDao()?.insert(newBestYield)!!
                }
            }

            val isBestYieldNeedToRefresh = oldBestYieldList?.equals(newBestYields) != true

            setHome(newProperties, newBestYields, isPropertiesNeedToRefresh, isBestYieldNeedToRefresh)
        }
    }

    private fun setHome(
        properties: List<PropertyEntity>?,
        bestYields: List<BestYieldEntity>?,
        isPropertiesNeedToRefresh: Boolean,
        isBestYieldNeedToRefresh: Boolean) {
        Utilities.log(Enums.LogType.Debug, TAG, "setHome()", showToast = false)
        val localHomeData = HomeModel(
            properties = properties,
            bestYields = bestYields,
            isPropertiesNeedToRefresh = isPropertiesNeedToRefresh,
            isBestYieldNeedToRefresh = isBestYieldNeedToRefresh,
        )
        this.homeCallback.postValue(localHomeData)
    }
    //endregion home
}