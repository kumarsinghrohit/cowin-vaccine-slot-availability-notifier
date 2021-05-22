package com.example.cowin.schedular;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.cowin.service.SendWhatsAppMessageService;
import com.example.cowin.service.WebClientService;

@Component
class VaccineNotifierScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(VaccineNotifierScheduler.class);
	private static final String[] PINCODELIST = { "411028" };
	private static final String DOSE_NUMBER = "2";

	private final SendWhatsAppMessageService sendWhatsAppMessageService;
	private final WebClientService webClientService;

	private String schedulerZoneId = "Asia/Kolkata";

	VaccineNotifierScheduler(SendWhatsAppMessageService sendWhatsAppMessageService, WebClientService webClientService) {
		this.sendWhatsAppMessageService = sendWhatsAppMessageService;
		this.webClientService = webClientService;
	}

	// This will hit covin at an interval of 10 minutes
	@Scheduled(cron = "0 */10 * * * ?", zone = "Asia/Kolkata")
	void findVaccineSlotsAvailability() throws IOException {
		LOGGER.info("************* VaccineNotifierScheduler has been started............... *************");
		List<String> message = new ArrayList<>();
		for (String pin : PINCODELIST) {
			String formattedDate = getCurrentDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
			List<String> response = webClientService.get(pin, formattedDate);
			for (String res : response) {
				message.addAll(prepareMessage(res));
			}
			sendWhatsAppMessageService.sendMessage(message);
		}
	}

	List<String> prepareMessage(String response) {
		List<String> messages = new ArrayList<>();
		try {
			JSONObject jsonObject = new JSONObject(response);
			JSONArray centers = (JSONArray) jsonObject.get("centers");
			(centers).forEach(center -> {
				JSONObject availableCenter = (JSONObject) center;
				JSONArray sessions = (JSONArray) (availableCenter).get("sessions");
				sessions.forEach(session -> {
					JSONObject givenSession = (JSONObject) session;
					int availableCapacity = ((int) givenSession.get("available_capacity_dose" + DOSE_NUMBER));
					if (availableCapacity > 0) {
						String message = "place:" + availableCenter.get("name") + "|" + "pincode:"
								+ availableCenter.get("pincode") + "|" + "date:" + givenSession.getString("date") + "|"
								+ "availableCapacity:" + availableCapacity + "|" + "vaccine:"
								+ givenSession.getString("vaccine") + "|" + "age:" + givenSession.get("min_age_limit");
						messages.add(message);
					}
				});
			});
		} catch (JSONException err) {
			LOGGER.error(err.toString());
		}
		return messages;
	}

	private LocalDate getCurrentDate() {
		ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(schedulerZoneId));
		return zonedDateTime.toLocalDate();
	}
}
