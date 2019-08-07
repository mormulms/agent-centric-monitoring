package org.esper.plugin;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface EsperService {
	
	@GET("esper/statement")
	Call<String> getAlerts();
	
	@POST("esper/statement/{statementName}")
	Call<String> addAlert(@Path("statementName") String statementName, @Body String statement);

	@DELETE("esper/statement/{statementName}")
	Call<String> removeAlert(@Path("statementName") String statementName);
}
