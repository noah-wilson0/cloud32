import com.example.childgps.api.CallRequest
import com.example.childgps.entity.LocationData
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @GET("locations")  // API Gateway의 엔드포인트 경로 설정
    suspend fun getAllLocations(
        @Query("action") action: String
    ): List<LocationData>

    // callbel 데이터를 삽입하는 POST 요청
    @POST("home")
    suspend fun insertCallValue(@Body body: CallRequest): Response<Void>
}
