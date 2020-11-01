package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.AddressService;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.businness.StateBusinessService;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("")
@CrossOrigin
public class AddressController {

    @Autowired
    CustomerService customerService;
    @Autowired
    AddressService addressBusinessService;

    @Autowired
    StateBusinessService stateBusinessService;

    /* The method handles Address save Related request.It takes the details as per in the SaveAddressRequest
     & produces response in SaveAddressResponse and returns UUID of newly Created Customer Address and Success message else Return error code and error Message.
      */
    @RequestMapping(method = RequestMethod.POST, path = "/address", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveAddressResponse> saveAddress(final SaveAddressRequest saveAddressRequest, @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, SaveAddressException, AddressNotFoundException {

        //Access the accessToken from the request Header
        String[] bearerToken = authorization.split("Bearer ");

        //Creating addressEntity from SaveAddressRequest data.
        AddressEntity addressEntity = new AddressEntity();

        addressEntity.setFlat_buil_number(saveAddressRequest.getFlatBuildingName());
        addressEntity.setCity(saveAddressRequest.getCity());
        addressEntity.setLocality(saveAddressRequest.getLocality());
        addressEntity.setPincode(saveAddressRequest.getPincode());
        addressEntity.setUuid(UUID.randomUUID().toString());
        addressEntity.setActive(1);

        final AddressEntity createdAddress = addressBusinessService.saveAddress(addressEntity, bearerToken[1], saveAddressRequest.getStateUuid());
        final CustomerAuthEntity customerAuthTokenEntity = customerService.getCustomer(bearerToken[1]);

        final CustomerAddressEntity customerAddressEntity = new CustomerAddressEntity();
        customerAddressEntity.setAddress(createdAddress);
        customerAddressEntity.setCustomer(customerAuthTokenEntity.getCustomer());
        addressBusinessService.saveCustomerAddress(customerAddressEntity);

        //Creating SaveAddressResponse response
        final SaveAddressResponse saveAddressResponse = new SaveAddressResponse().id(createdAddress.getUuid()).status("ADDRESS SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SaveAddressResponse>(saveAddressResponse, HttpStatus.OK);
    }

    /*  The method handles get all Address  request.It takes the authorization
         & produces response in AddressListResponse and returns list of Customer Address .If error Return error code and error Message.
          */
    @RequestMapping(method = RequestMethod.GET, path = "/address/customer", produces = MediaType.APPLICATION_JSON_UTF8_VALUE, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<AddressListResponse>> getAllSavedAddress(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {

        String[] bearerToken = authorization.split("Bearer ");

        List<CustomerAddressEntity> customerAddressEntityList = addressBusinessService.getAddressByCustomer(bearerToken[1]);
        List<AddressListResponse> addressListResponses = new ArrayList<>();

        for (CustomerAddressEntity cae : customerAddressEntityList) {
            AddressEntity addressEntity = cae.getAddress();
            AddressListState addressListState = new AddressListState().id(UUID.fromString(addressEntity.getState_id().getUuid())).stateName(addressEntity.getState_id().getState_name());
            AddressList addressList = new AddressList().id(UUID.fromString(addressEntity.getUuid()))
                    .flatBuildingName(addressEntity.getFlat_buil_number()).locality(addressEntity.getLocality())
                    .city(addressEntity.getCity()).pincode(addressEntity.getPincode()).state(addressListState);
            AddressListResponse addressListResponse = new AddressListResponse().addAddressesItem(addressList);
            addressListResponses.add(addressListResponse);
        }
        return new ResponseEntity<List<AddressListResponse>>(addressListResponses, HttpStatus.OK);
    }

    /*  The method handles delete  Address  request.It takes the authorization and path variables address UUID
  & produces response in DeleteAddressResponse and returns UUID of deleted address and Successfull message .If error Return error code and error Message.
   */
    @RequestMapping(method = RequestMethod.DELETE, path = "/address/{address_id}", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<DeleteAddressResponse> deleteSavedAddress(@RequestHeader("authorization") final String authorization, @PathVariable(value = "address_id") final String addressUuid) throws AuthorizationFailedException, AddressNotFoundException {

        String[] accessToken = authorization.split("Bearer ");

        final String deleteAddress = addressBusinessService.deleteAddress(addressUuid, accessToken[1]);

        DeleteAddressResponse deleteAddressResponse = new DeleteAddressResponse().id(UUID.fromString(deleteAddress)).status("Address Deleted");

        return new ResponseEntity<DeleteAddressResponse>(deleteAddressResponse, HttpStatus.OK);
    }

    /*  The method handles States request.It produces response in StatesListResponse and returns UUID & stateName .If error Return error code and error Message.
     */
    @RequestMapping(method = RequestMethod.GET, path = "/states", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<StatesListResponse>> getAllStates() {
        List<StateEntity> stateEntities = stateBusinessService.getAllStates();

        List<StatesListResponse> states = new ArrayList<>();
        for (StateEntity state : stateEntities) {
            UUID stateUuid = UUID.fromString(state.getUuid());
            StatesList statesList = new StatesList().id(stateUuid).stateName(state.getState_name());
            StatesListResponse statesListResponse = new StatesListResponse().addStatesItem(statesList);
            states.add(statesListResponse);
        }
        return  new ResponseEntity<List<StatesListResponse>>(states, HttpStatus.OK);
    }
}
