package org.mapper.load_generator;

import java.io.IOException;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Hello world!
 *
 */
public class LoadGenerator {
	
	private static final String TELEGRAF_URL = "http://127.0.0.1:9998";
	private static int min = 10;
	private static int max = 30;
	private static String measurement = "test";
	private static String field = "number";
	
	public static void main(String[] args) {

		if (args.length==2) {
			min = Integer.parseInt(args[0]);
			max = Integer.parseInt(args[1]);
		}
		System.out.println("generating values between "+min+" and "+max);
		
		
		HttpUrl httpUrl = HttpUrl.get(TELEGRAF_URL);
		Retrofit retrofit = new Retrofit.Builder().baseUrl(httpUrl).addConverterFactory(ScalarsConverterFactory.create()).build();
		LoadGeneratorService service = retrofit.create(LoadGeneratorService.class);
		
		int value = min;
		boolean risingValue = true;
		
		while (true) {
			if (value == min) {
				risingValue = true;
			} else if (value == max) {
				risingValue = false;
			}
			String series = buildSeries(value);
			System.out.println("sending: "+ series);
			Call<String> callPostSeries = service.postSeries(series);
			try {
				Response<String> responsePostSeries = callPostSeries.execute();
				if (responsePostSeries.isSuccessful()) {
					System.out.println("success");
				}
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (risingValue) {
				value++;
			} else {
				value--;
			}
		}
		
	}
	
	public static String buildSeries(int value) {
		return measurement+" "+field+"="+value;
	}
}
