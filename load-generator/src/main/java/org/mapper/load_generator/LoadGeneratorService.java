package org.mapper.load_generator;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LoadGeneratorService {
	
	@POST("telegraf")
	Call<String> postSeries(@Body String series);

}
