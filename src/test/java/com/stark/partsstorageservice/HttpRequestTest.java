package com.stark.partsstorageservice;

import static org.assertj.core.api.Assertions.assertThat;

import com.stark.parts_storage.AddInventoryRequest;
import com.stark.parts_storage.InventoryQueryResponse;
import com.stark.parts_storage.InventoryReservationRequest;
import com.stark.parts_storage.InventoryReservationResponse;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class HttpRequestTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testAddInventory() {
        AddInventoryRequest air = new AddInventoryRequest();
        air.setPartCode("A");
        air.setQuantity(1);

        HttpEntity<AddInventoryRequest> request = new HttpEntity<>(air);

        ResponseEntity<String> result = this.restTemplate.postForEntity("http://localhost:" + port + "/v1/inventory",
                request, String.class);

        assertThat(result.getStatusCodeValue() == 201);
    }

    @Test
    public void testRequestInventory() {
        AddInventoryRequest air = new AddInventoryRequest();
        air.setPartCode("A");
        air.setQuantity(1);

        HttpEntity<AddInventoryRequest> request = new HttpEntity<>(air);
        ResponseEntity<String> result = this.restTemplate.postForEntity("http://localhost:" + port + "/v1/inventory",
                request, String.class);
        assertThat(result.getStatusCodeValue() == 201);

        ResponseEntity<InventoryQueryResponse> getResult = this.restTemplate
                .getForEntity("http://localhost:" + port + "/v1/inventory?partCode=A", InventoryQueryResponse.class);
        assertThat(getResult.getStatusCodeValue() == 200);
        assertThat(getResult.getBody().getAvailableQuantity() == 1);
    }

    @Test
    public void testRequestInventoryNotAvailable() {
        ResponseEntity<InventoryQueryResponse> getResult = this.restTemplate
                .getForEntity("http://localhost:" + port + "/v1/inventory?partCode=B", InventoryQueryResponse.class);
        assertThat(getResult.getStatusCodeValue() == 200);
        assertThat(getResult.getBody().getAvailableQuantity() == 0);
    }

    @Test
    public void reserveInventory() {
        AddInventoryRequest air = new AddInventoryRequest();
        air.setPartCode("A");
        air.setQuantity(1);

        HttpEntity<AddInventoryRequest> request = new HttpEntity<>(air);
        ResponseEntity<String> result = this.restTemplate.postForEntity("http://localhost:" + port + "/v1/inventory",
                request, String.class);
        assertThat(result.getStatusCodeValue() == 201);

        InventoryReservationRequest inventoryRequest = new InventoryReservationRequest();
        inventoryRequest.setRepairRequestId("A-1");
        inventoryRequest.setPartCode("A");
        inventoryRequest.setQuantity(1);
        HttpEntity<InventoryReservationRequest> reservationRequest = new HttpEntity<>(inventoryRequest);

        ResponseEntity<InventoryReservationResponse> reservationResult = this.restTemplate.postForEntity(
                "http://localhost:" + port + "/v1/inventory/reservation", reservationRequest, InventoryReservationResponse.class);

        assertThat(reservationResult.getStatusCodeValue() == 201);
        assertThat(reservationResult.getBody().getReservationId() != null);
        assertThat(reservationResult.getBody().getRemainingParts() == 0);
    }

    @Test
    public void reserveInventoryNotAvailable() {
        AddInventoryRequest air = new AddInventoryRequest();
        air.setPartCode("A");
        air.setQuantity(1);

        HttpEntity<AddInventoryRequest> request = new HttpEntity<>(air);
        ResponseEntity<String> result = this.restTemplate.postForEntity("http://localhost:" + port + "/v1/inventory",
                request, String.class);
        assertThat(result.getStatusCodeValue() == 201);

        InventoryReservationRequest inventoryRequest = new InventoryReservationRequest();
        inventoryRequest.setRepairRequestId("A-1");
        inventoryRequest.setPartCode("B");
        inventoryRequest.setQuantity(1);
        HttpEntity<InventoryReservationRequest> reservationRequest = new HttpEntity<>(inventoryRequest);

        ResponseEntity<InventoryReservationResponse> reservationResult = this.restTemplate.postForEntity(
                "http://localhost:" + port + "/v1/inventory/reservation", reservationRequest, InventoryReservationResponse.class);

        assertThat(reservationResult.getStatusCodeValue() == 201);
        assertThat(reservationResult.getBody() == null);
    }

    @Test
    public void reserveInventoryNotEnough() {
        AddInventoryRequest air = new AddInventoryRequest();
        air.setPartCode("A");
        air.setQuantity(1);

        HttpEntity<AddInventoryRequest> request = new HttpEntity<>(air);
        ResponseEntity<String> result = this.restTemplate.postForEntity("http://localhost:" + port + "/v1/inventory",
                request, String.class);
        assertThat(result.getStatusCodeValue() == 201);

        InventoryReservationRequest inventoryRequest = new InventoryReservationRequest();
        inventoryRequest.setRepairRequestId("A-1");
        inventoryRequest.setPartCode("A");
        inventoryRequest.setQuantity(2);
        HttpEntity<InventoryReservationRequest> reservationRequest = new HttpEntity<>(inventoryRequest);

        ResponseEntity<InventoryReservationResponse> reservationResult = this.restTemplate.postForEntity(
                "http://localhost:" + port + "/v1/inventory/reservation", reservationRequest, InventoryReservationResponse.class);

        assertThat(reservationResult.getStatusCodeValue() == 201);
        assertThat(reservationResult.getBody() == null);
    }

}
