package com.google.sps;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.ZonedDateTime;

public class JsonConfig {
	public static Gson configureGson() {
		return new GsonBuilder()
				.registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeTypeAdapter())
				.create();
	}

	private static class ZonedDateTimeTypeAdapter extends TypeAdapter<ZonedDateTime> {
		@Override
		public void write(JsonWriter out, ZonedDateTime value) throws IOException {
			out.value(value.toString());
		}

		@Override
		public ZonedDateTime read(JsonReader in) throws IOException {
			return ZonedDateTime.parse(in.nextString());
		}
	}
}
