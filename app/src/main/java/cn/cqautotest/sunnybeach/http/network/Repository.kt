package cn.cqautotest.sunnybeach.http.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataScope
import androidx.lifecycle.liveData
import cn.cqautotest.sunnybeach.db.dao.PlaceDao
import cn.cqautotest.sunnybeach.execption.NotLoginException
import cn.cqautotest.sunnybeach.execption.ServiceException
import cn.cqautotest.sunnybeach.manager.UserManager
import cn.cqautotest.sunnybeach.model.*
import cn.cqautotest.sunnybeach.model.wallpaper.WallpaperBean
import cn.cqautotest.sunnybeach.model.weather.Place
import cn.cqautotest.sunnybeach.model.weather.Weather
import cn.cqautotest.sunnybeach.util.lowercaseMd5
import cn.cqautotest.sunnybeach.util.toJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import java.io.File

/**
 *    author : A Lonely Cat
 *    github : https://github.com/anjiemo/SunnyBeach
 *    time   : 2021/09/07
 *    desc   : 存储库
 */
object Repository {

    private const val NOT_LOGIN_CODE = 11126

    private val cachePhotoIdList = arrayListOf<WallpaperBean.Res.Vertical>()

    fun getCoursePlayAuth(videoId: String) = liveData(Dispatchers.IO) {
        launchAndGetData { CourseNetwork.getCoursePlayAuth(videoId) }
    }

    fun modifyPasswordBySms(smsCode: String, user: User) = liveData(Dispatchers.IO) {
        launchAndGetMsg { UserNetwork.modifyPasswordBySms(smsCode, user) }
    }

    fun modifyPasswordByOldPwd(modifyPwd: ModifyPwd) = liveData(Dispatchers.IO) {
        launchAndGetMsg { UserNetwork.modifyPasswordByOldPwd(modifyPwd) }
    }

    fun checkSmsCode(phoneNumber: String, smsCode: String) = liveData(Dispatchers.IO) {
        launchAndGetMsg { UserNetwork.checkSmsCode(phoneNumber, smsCode) }
    }

    fun sendForgetSmsVerifyCode(smsInfo: SmsInfo) = liveData(Dispatchers.IO) {
        launchAndGetMsg { UserNetwork.sendForgetSmsVerifyCode(smsInfo) }
    }

    fun registerAccount(smsCode: String, user: User) = liveData(Dispatchers.IO) {
        launchAndGetMsg { UserNetwork.registerAccount(smsCode, user) }
    }

    fun sendRegisterSmsVerifyCode(smsInfo: SmsInfo) = liveData(Dispatchers.IO) {
        launchAndGetMsg { UserNetwork.sendRegisterSmsVerifyCode(smsInfo) }
    }

    fun getSobIEDetailList(userId: String, page: Int) = liveData(Dispatchers.IO) {
        launchAndGetData { UserNetwork.getSobIEDetailList(userId, page) }
    }

    fun getVipUserList() = liveData(Dispatchers.IO) {
        launchAndGetData { UserNetwork.getVipUserList() }
    }

    fun getAchievement() = liveData(Dispatchers.IO) {
        launchAndGetData { UserNetwork.getAchievement() }
    }

    fun queryTotalSobCount() = liveData(Dispatchers.IO) {
        launchAndGetData { UserNetwork.queryTotalSobCount() }
    }

    fun queryUserInfo() = liveData(Dispatchers.IO) {
        launchAndGetData { UserNetwork.queryUserInfo() }
    }

    fun getAllowance() = liveData(Dispatchers.IO) {
        val result = try {
            coroutineScope {
                val result = UserNetwork.getAllowance()
                Timber.d("result is $result")
                when (result.getCode()) {
                    // 未领取VIP津贴
                    11129 -> Result.success(false)
                    // 已经领取了VIP津贴
                    11128 -> Result.success(true)
                    else -> Result.failure(ServiceException(result.getMessage()))
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.failure(t)
        }
        emit(result)
    }

    fun checkAllowance() = liveData(Dispatchers.IO) {
        val result = try {
            coroutineScope {
                val result = UserNetwork.checkAllowance()
                Timber.d("result is $result")
                when (result.getCode()) {
                    // 未领取VIP津贴
                    11129 -> Result.success(false)
                    // 已经领取了VIP津贴
                    11128 -> Result.success(true)
                    else -> Result.failure(ServiceException(result.getMessage()))
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.failure(t)
        }
        emit(result)
    }

    fun unfollowUser(userId: String) = liveData(Dispatchers.IO) {
        launchAndGetData { UserNetwork.unfollowUser(userId) }
    }

    fun followUser(userId: String) = liveData(Dispatchers.IO) {
        launchAndGetData { UserNetwork.followUser(userId) }
    }

    fun followState(userId: String) = liveData(Dispatchers.IO) {
        launchAndGetData { UserNetwork.followState(userId) }
    }

    fun getUserInfo(userId: String) = liveData(Dispatchers.IO) {
        launchAndGetData { UserNetwork.getUserInfo(userId) }
    }

    fun getAchievement(userId: String) = liveData(Dispatchers.IO) {
        launchAndGetData { UserNetwork.getAchievement(userId) }
    }

    fun logout() = liveData(Dispatchers.IO) {
        val result = try {
            coroutineScope {
                val result = UserNetwork.logout()
                Timber.d("result is $result")
                System.currentTimeMillis()
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            System.currentTimeMillis()
        }
        emit(result)
    }

    suspend fun checkToken() = try {
        val result = UserNetwork.checkToken()
        if (result.isSuccess()) {
            val userBasicInfo = result.getData()
            UserManager.saveUserBasicInfo(userBasicInfo)
            UserManager.setupAutoLogin(true)
            userBasicInfo
        } else throw ServiceException()
    } catch (t: Throwable) {
        t.printStackTrace()
        UserManager.saveUserBasicInfo(null)
        UserManager.setupAutoLogin(false)
        null
    }

    suspend fun login(userAccount: String, password: String, captcha: String) = try {
        val user = User(userAccount, password.lowercaseMd5)
        val result = UserNetwork.login(captcha, user)
        if (result.isSuccess()) {
            val userBasicInfo = checkToken()
            // 将基本信息缓存到本地
            UserManager.saveUserBasicInfo(userBasicInfo)
            UserManager.setupAutoLogin(userBasicInfo != null)
            userBasicInfo ?: throw NotLoginException()
        } else throw ServiceException()
    } catch (t: Throwable) {
        t.printStackTrace()
        UserManager.setupAutoLogin(false)
        null
    }

    fun queryUserAvatar(account: String) = liveData(Dispatchers.IO) {
        val result = try {
            coroutineScope {
                val result = UserNetwork.queryUserAvatar(account)
                Timber.d("result is $result")
                if (result.isSuccess()) result.getData()
                else ""
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            ""
        }
        emit(result)
    }

    fun checkAppUpdate(): LiveData<Result<AppUpdateInfo>> = liveData(Dispatchers.IO) {
        launchAndGetData { AppNetwork.checkAppUpdate() }
    }

    fun getMourningCalendar(): LiveData<Result<List<MourningCalendar>>> = liveData(Dispatchers.IO) {
        launchAndGetData { AppNetwork.getMourningCalendar() }
    }

    fun setPhotoIdList(photoIdList: List<WallpaperBean.Res.Vertical>) {
        if (photoIdList !== cachePhotoIdList) {
            cachePhotoIdList.clear()
            if (photoIdList.isNullOrEmpty().not()) {
                cachePhotoIdList.addAll(photoIdList)
            }
        } else {
            if (photoIdList.isNullOrEmpty().not()) {
                val newList = photoIdList.toList()
                cachePhotoIdList.clear()
                cachePhotoIdList.addAll(newList)
            } else {
                cachePhotoIdList.clear()
            }
        }
    }

    fun getPhotoList() = cachePhotoIdList.toList()

    fun getFishCommendListById(momentId: String, page: Int) = liveData(Dispatchers.IO) {
        launchAndGetData { FishNetwork.getFishCommendListById(momentId, page) }
    }

    suspend fun uploadFishImage(imageFile: File): String? {
        return try {
            val fileName = imageFile.name
            Timber.d("===> fileName is $fileName")
            val requestBody = RequestBody.create("image/png".toMediaType(), imageFile)
            val part = MultipartBody.Part.createFormData("image", fileName, requestBody)
            val result = FishNetwork.uploadFishImage(part)
            Timber.d("result is ${result.toJson()}")
            // 此处不能返回 Result<T> ，详见：https://github.com/mockk/mockk/issues/443
            // Result getOrNull give ClassCastException：https://stackoverflow.com/questions/68016267/result-getornull-give-classcastexception
            if (result.isSuccess()) result.getData()
            else null
        } catch (t: Throwable) {
            t.printStackTrace()
            null
        }
    }

    fun putFish(moment: Map<String, Any?>) = liveData(Dispatchers.IO) {
        launchAndGetData { FishNetwork.putFish(moment) }
    }

    fun loadFishDetailById(momentId: String) = liveData(Dispatchers.IO) {
        launchAndGetData { FishNetwork.loadFishDetailById(momentId) }
    }

    fun loadWallpaperBannerList() = liveData(Dispatchers.IO) {
        val result = try {
            coroutineScope {
                val result = PhotoNetwork.loadWallpaperBannerList()
                Timber.d("result is $result")
                if ("0" == result.errno) Result.success(result.data)
                else Result.failure(ServiceException())
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.failure(t)
        }
        emit(result)
    }

    fun postComment(momentComment: Map<String, Any?>, isReply: Boolean) = liveData(Dispatchers.IO) {
        launchAndGetData {
            if (isReply) FishNetwork.replyComment(momentComment)
            else FishNetwork.postComment(momentComment)
        }
    }

    fun dynamicLikes(momentId: String) = liveData(Dispatchers.IO) {
        launchAndGetMsg { FishNetwork.dynamicLikes(momentId) }
    }

    fun loadTopicList() = liveData(Dispatchers.IO) {
        launchAndGetData { FishNetwork.loadTopicList() }
    }

    fun getRichList() = liveData(Dispatchers.IO) {
        launchAndGetData { UserNetwork.getRichList() }
    }

    fun readQaMsg(msgId: String) = liveData(Dispatchers.IO) {
        launchAndGetMsg { MsgNetwork.readQaMsg(msgId) }
    }

    fun readAtMeMsg(msgId: String) = liveData(Dispatchers.IO) {
        launchAndGetMsg { MsgNetwork.readAtMeMsg(msgId) }
    }

    fun readMomentMsg(msgId: String) = liveData(Dispatchers.IO) {
        launchAndGetMsg { MsgNetwork.readMomentMsg(msgId) }
    }

    /**
     * 更新文章消息为：已回复
     */
    fun replyArticleMsg(msgId: String) = liveData(Dispatchers.IO) {
        launchAndGetMsg { MsgNetwork.readArticleMsg(msgId, 2) }
    }

    /**
     * 更新文章消息为：已读
     */
    fun readArticleMsg(msgId: String) = liveData(Dispatchers.IO) {
        launchAndGetMsg { MsgNetwork.readArticleMsg(msgId, 1) }
    }

    /**
     * 一键已读所有消息
     */
    fun readAllMsg() = liveData(Dispatchers.IO) {
        launchAndGetMsg { MsgNetwork.readAllMsg() }
    }

    /**
     * 获取未读消息数量
     */
    fun getUnReadMsgCount() = liveData(Dispatchers.IO) {
        launchAndGetData { MsgNetwork.getUnReadMsgCount() }
    }

    fun searchPlaces(query: String) = liveData(Dispatchers.IO) {
        Timber.d(query)
        val result = try {
            val placeResponse = WeatherNetwork.searchPlace(query)
            Timber.d("${placeResponse.status}|${placeResponse.places[0].name}")
            if (placeResponse.status == "ok") {
                Timber.d(placeResponse.status)
                val places = placeResponse.places
                Result.success(places)
            } else {
                Result.failure(RuntimeException("response status is ${placeResponse.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
        emit(result)
    }

    /**
     * 刷新天气
     */
    fun refreshWeather(lng: String, lat: String) = liveData(Dispatchers.IO) {
        val result = try {
            coroutineScope {
                val deferredRealtime = async {
                    WeatherNetwork.getRealtimeWeather(lng, lat)
                }
                val deferredDaily = async {
                    WeatherNetwork.getDailyWeather(lng, lat)
                }
                val realtimeResponse = deferredRealtime.await()
                val dailyResponse = deferredDaily.await()
                if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                    val weather = Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
                    Result.success(weather)
                } else {
                    Result.failure(
                        RuntimeException(
                            "realtimeWeather status is ${realtimeResponse.status}\n" +
                                    "dailyWeather status is ${dailyResponse.status}"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
        emit(result)
    }

    /**
     * 保存地点
     */
    fun savePlace(place: Place) = PlaceDao.savePlace(place)

    /**
     * 获取保存的地点
     */
    fun getSavedPlace() = PlaceDao.getSavedPlace()

    /**
     * 是否保存了地点
     */
    fun isSaved() = PlaceDao.isSaved()

    /**
     * 启动并获取数据
     */
    private suspend inline fun <reified T> LiveDataScope<Result<T>>.launchAndGetData(crossinline action: suspend () -> ApiResponse<T>) {
        launchAndGet(action = action, onSuccess = { it.getData() })
    }

    /**
     * 启动并获取消息
     */
    private suspend inline fun LiveDataScope<Result<Any>>.launchAndGetMsg(crossinline action: suspend () -> ApiResponse<Any>) {
        launchAndGet(action = action, onSuccess = { it.getMessage() })
    }

    /**
     * 启动并获取
     * 成功时返回：Result.success()
     * 失败时返回：Result.failure()
     */
    private suspend inline fun <reified T> LiveDataScope<Result<T>>.launchAndGet(
        // 需要调用的接口
        crossinline action: suspend () -> ApiResponse<T>,
        // 请求成功时的回调
        crossinline onSuccess: (ApiResponse<T>) -> T
    ) {
        val result = try {
            coroutineScope {
                val result = action.invoke()
                Timber.d("result is ${result.toJson()}")
                if (result.isSuccess()) Result.success(onSuccess.invoke(result))
                else when (result.getCode()) {
                    NOT_LOGIN_CODE -> Result.failure(NotLoginException(result.getMessage()))
                    else -> Result.failure(ServiceException(result.getMessage()))
                }
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.failure(t)
        }
        emit(result)
    }
}