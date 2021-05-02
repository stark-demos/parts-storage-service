package com.stark.partsstorageservice;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.stark.parts_storage.AddInventoryRequest;
import com.stark.parts_storage.InventoryQueryResponse;
import com.stark.parts_storage.InventoryReservationRequest;
import com.stark.parts_storage.InventoryReservationResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PartsStorageServiceController {
    private static Logger logger = LoggerFactory.getLogger(PartsStorageServiceController.class);
    private Map<String, Integer> inventory = new ConcurrentHashMap<>();

    @GetMapping("/v2/inventory")
    public InventoryQueryResponse getInventory(
            @RequestParam(value = "partCode", defaultValue = "NONE") String partCode) {
        logger.debug("Requesting inventory status for partCode {}", partCode);
        InventoryQueryResponse response = new InventoryQueryResponse();
        response.setPartCode(partCode);
        if (inventory.containsKey(partCode)) {
            logger.debug("partCode {} found at inventory map", partCode);
            response.setAvailableQuantity(inventory.get(partCode));
        } else {
            response.setAvailableQuantity(0);
        }
        logger.info("Inventory found {}", response);
        return response;
    }

    @PostMapping("/v2/inventory")
    public String addInventory(@RequestBody AddInventoryRequest model) {
        logger.debug("Requested to add inventory {}", model);
        String partCode = model.getPartCode();
        Integer quantity = model.getQuantity();
        if (inventory.containsKey(partCode)) {
            Integer currentValue = inventory.get(partCode);
            logger.debug("Override current inventory amount {} with {}", currentValue, currentValue + quantity);
            inventory.put(partCode, currentValue + quantity);
        } else {
            logger.debug("previous inventory not found for code {}, setting initial quantity as {}", partCode,
                    quantity);
            inventory.put(partCode, quantity);
        }
        return "OK";
    }

    @PostMapping("/v2/inventory/reservation")
    public InventoryReservationResponse reserveInventory(@RequestBody InventoryReservationRequest request) {
        logger.debug("Requested to reserve inventory {}", request);
        String partCode = request.getPartCode();
        String branchCode = request.getBranchCode();
        Integer quantity = request.getQuantity();
        Integer remainingParts = 0;

        if (branchCode == null || "".equals(branchCode) || "null".equals(branchCode)) {
            logger.warn("Branch not found in reservation request {}", request);
            return null;
        }

        if (inventory.containsKey(partCode)) {
            Integer currentValue = inventory.get(partCode);
            logger.debug("Found {} parts in inventory for {}", currentValue, partCode);

            remainingParts = currentValue - quantity;
            if (remainingParts >= 0) {
                logger.debug("Override current inventory amount {} with {}", currentValue, remainingParts);
                inventory.put(partCode, remainingParts);
            } else {
                logger.warn("Not enough parts in inventory for {} with requestid {}", partCode,
                        request.getRepairRequestId());
                return null;
            }
        } else {
            logger.debug("previous inventory not found for code {}, unable to assign quantity of {}", partCode,
                    quantity);
            return null;
        }
        InventoryReservationResponse response = new InventoryReservationResponse();
        response.setBranchCode(branchCode);
        response.setPartCode(request.getPartCode());
        response.setQuantity(request.getQuantity());
        response.setRepairRequestId(request.getRepairRequestId());
        response.setRemainingParts(remainingParts);
        response.setReservationId(UUID.randomUUID().toString());
        return response;
    }

}
