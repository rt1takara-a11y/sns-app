package com.example.snsapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class SnsappApplication {

	private static final Logger logger = LoggerFactory.getLogger(SnsappApplication.class);

	public static void main(String[] args) {
		try {
			SpringApplication.run(SnsappApplication.class, args);
		} catch (Throwable t) {
			// 起動時の例外を必ず出力して原因を特定しやすくする
			logger.error("Application failed to start", t);
			t.printStackTrace();
			// 明示的に非ゼロで終了（Gradleの bootRun に原因を残す）
			System.exit(1);
		}
	}

}
