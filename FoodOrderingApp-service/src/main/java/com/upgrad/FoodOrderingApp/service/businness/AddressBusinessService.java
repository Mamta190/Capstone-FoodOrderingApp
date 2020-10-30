package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.AddressDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthTokenEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public class AddressBusinessService {

    @Autowired
    AddressDao addressDao; //Handle all data of Address

    @Autowired
    CustomerDao customerDao; //Handle all data customer

    /* This method is to saveAddress.Takes the Address and state entity and saves the Address to the DB.
    If error throws exception with error code and error message.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity saveAddress(AddressEntity addressEntity, String stateEntity, final String authorizationToken) throws SaveAddressException, AuthorizationFailedException {

        if (addressEntity.getCity() == null || addressEntity.getFlat_buil_number() == null || addressEntity.getPincode() == null || addressEntity.getLocality() == null) {
            throw new SaveAddressException("SAR-001", "No field can be empty");
        }
        if (!addressDao.IsPinCodeValid(addressEntity.getPincode())) {
            throw new SaveAddressException("SAR-002", "Invalid pincode");
        }
        if (getStateByUuid(addressEntity.getUuid()) == null) {
            throw new SaveAddressException("ANF-002", "No state by this id");
        }
        CustomerAuthTokenEntity customerAuthTokenEntity = customerDao.checkAuthToken(authorizationToken);

        if (customerAuthTokenEntity.equals(null)) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }

        final ZonedDateTime customerSignOut = customerAuthTokenEntity.getLogoutAt();

        if (customerSignOut != null && customerAuthTokenEntity != null) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        }
        final ZonedDateTime customerSessionExpireTime = customerAuthTokenEntity.getExpiresAt();
        ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.systemDefault());
        if (customerSessionExpireTime.compareTo(currentTime) < 0) {
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }
        return addressDao.saveAddress(addressEntity);
    }

    //To get state by UUID
    private StateEntity getStateByUuid(String uuid) {
        StateEntity stateEntity = addressDao.getState(uuid);
        return stateEntity;
    }
    //To save the customer address
    public CustomerAddressEntity saveCustomerAddress(CustomerAddressEntity customerAddressEntity){

        return addressDao.save(customerAddressEntity);
    }
}