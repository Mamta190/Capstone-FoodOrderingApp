package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.*;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.UUID;

@RestController
@RequestMapping("")
@CrossOrigin
public class CustomerController {
    @Autowired
    CustomerService customerService;

    @Autowired
    UpdatedCustomerBusinessService updatedCustomerBusinessService;

    @Autowired
    UpdatePasswordBusinessService updatePasswordBusinessService;

    /* The method handles Customer SignUp Related request.It takes the details as per in the SignupCustomerRequest
    & produces response in SignupCustomerResponse and returns UUID of newly Created Customer and Success message else Return error code and error Message.
     */
    @RequestMapping(method = RequestMethod.POST, path = "/customer/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupCustomerResponse> signup(@RequestBody(required = false) final SignupCustomerRequest signupCustomerRequest) throws SignUpRestrictedException {
        CustomerEntity customerEntity = new CustomerEntity();
        customerEntity.setUuid(UUID.randomUUID().toString());
        customerEntity.setEmail(signupCustomerRequest.getEmailAddress());
        customerEntity.setFirstname(signupCustomerRequest.getFirstName());
        customerEntity.setLastname(signupCustomerRequest.getLastName());
        customerEntity.setPassword(signupCustomerRequest.getPassword());
        customerEntity.setContactNumber(signupCustomerRequest.getContactNumber());
        CustomerEntity createdCustomer = customerService.saveCustomer(customerEntity);

        SignupCustomerResponse signupCustomerResponse = new SignupCustomerResponse().id(createdCustomer.getUuid()).status("CUSTOMER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SignupCustomerResponse>(signupCustomerResponse, HttpStatus.CREATED);
    }

    /*This Method handles the Login request and takes authorization parameter in Base64 coded and produces a LoginResponse containing info customer
    and response header containing bearer accessToken. If error returns the error code with corresponding Message.
     */
    @RequestMapping(method = RequestMethod.POST, path = "/customer/login", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LoginResponse> login(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException {
        byte[] decode = Base64.getDecoder().decode(authorization.split(" ")[1]);
        String decodedText = new String(decode);
        if (decodedText.indexOf(":") == -1){
            throw new AuthenticationFailedException("ATH-003", "Incorrect format of decoded customer name and password");
        }
        String[] decodedArray = decodedText.split(":");

        CustomerAuthEntity customerAuthTokenEntity = customerService.authenticate(decodedArray[0], decodedArray[1]);
        CustomerEntity customerEntity = customerAuthTokenEntity.getCustomer();
        LoginResponse loginResponse = new LoginResponse().id(customerEntity.getUuid()).message("LOGGED IN SUCCESSFULLY");
        HttpHeaders headers = new HttpHeaders();
        headers.add("accessToken", customerAuthTokenEntity.getAccessToken());
        return new ResponseEntity<LoginResponse>(loginResponse, headers, HttpStatus.OK);
    }

    /* This method handles the customer logout ,It takes the Bearer accessToken from authorization in the header and logs out the customer
    and returns a LogoutResponse conatining UUID of customer and the successful message.If error returns the error code with corresponding Message.
     */
    @RequestMapping(method = RequestMethod.POST, path = "/customer/logout", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LogoutResponse> logOut(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException, AuthorizationFailedException {
        CustomerEntity logOutUser = customerService.logOut(authorization);
        LogoutResponse logoutResponse = new LogoutResponse().id(logOutUser.getUuid()).message("LOGGED OUT SUCCESSFULLY");
        return new ResponseEntity<LogoutResponse>(logoutResponse, HttpStatus.OK);
    }

    /* This method handles the customer details update request. Takes the request as UpdateCustomerRequest and produces UpdateCustomerResponse containing the details of the updated Customer.
    If error returns the error code with corresponding Message.
     */
    @RequestMapping(method = RequestMethod.PUT, path = "/customer", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdateCustomerResponse> updateCustomer(@RequestHeader("authorization") final String authorization, @RequestBody(required = false) UpdateCustomerRequest updateCustomerRequest) throws AuthorizationFailedException, UpdateCustomerException {

        String[] authorizationData = authorization.split("Bearer");
        String userAccessToken = authorizationData[0];
        String firstName = updateCustomerRequest.getFirstName();
        String lastName = updateCustomerRequest.getLastName();
        CustomerEntity updateCustomer = updatedCustomerBusinessService.customerToBeUpdated(userAccessToken, firstName, lastName);
        UpdateCustomerResponse updateCustomerResponse = new UpdateCustomerResponse().id(updateCustomer.getUuid()).firstName(updateCustomer.getFirstname()).lastName(updateCustomer.getLastname())
                .status("CUSTOMER DETAILS UPDATED SUCCESSFULLY");
        return new ResponseEntity<UpdateCustomerResponse>(updateCustomerResponse, HttpStatus.OK);
    }
        /* This method is to updateCustomerPassword the customer using oldPassword,newPassword & customerEntity and return the CustomerEntity .
     If error throws exception with error code and error message.
     */

    @RequestMapping(method = RequestMethod.PUT, path = "/customer/password", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdatePasswordResponse> updatePassword(@RequestHeader("authorization") final String accessToken, @RequestBody(required = false) UpdatePasswordRequest updatePasswordRequest ) throws AuthorizationFailedException, UpdateCustomerException{
        String[] authorizedData = accessToken.split(" ");
        String userAccessToken = authorizedData[1];
        String newPassword = updatePasswordRequest.getNewPassword();
        String oldPassword = updatePasswordRequest.getOldPassword();
        CustomerEntity customerEntity = updatePasswordBusinessService.updatedPassword(userAccessToken, newPassword, oldPassword);
        UpdatePasswordResponse updatePasswordResponse = new UpdatePasswordResponse().id(customerEntity.getUuid())
                .status("CUSTOMER PASSWORD UPDATED SUCCESSFULLY");
        return new ResponseEntity<UpdatePasswordResponse>(updatePasswordResponse, HttpStatus.OK);
    }
}
