package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.AddressDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerAddressDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.AddressNotFoundException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SaveAddressException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Service
public class AddressService {

    @Autowired
    AddressDao addressDao; //Handle all data of Address

    @Autowired
    CustomerDao customerDao; //Handle all data customer

    @Autowired
    CustomerAddressDao customerAddressDao;

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
        CustomerAuthEntity customerAuthTokenEntity = customerDao.checkAuthToken(authorizationToken);

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
    public CustomerAddressEntity saveCustomerAddress(CustomerAddressEntity customerAddressEntity) {

        return addressDao.save(customerAddressEntity);
    }

    //To get Address By Customer
    @Transactional(propagation = Propagation.REQUIRED)
    public List<CustomerAddressEntity> getAddressByCustomer(final String authorizationToken) throws AuthorizationFailedException {

        CustomerAuthEntity customerAuth = customerDao.checkAuthToken(authorizationToken);

        if (customerAuth.equals(null)) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }
        final ZonedDateTime customerSignOutTime = customerAuth.getLogoutAt();

        if (customerSignOutTime != null && customerAuth != null) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        }

        final ZonedDateTime customerSessionExpireTime = customerAuth.getExpiresAt();
        ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.systemDefault());
        if (customerSessionExpireTime.compareTo(currentTime) < 0) {
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }

        List<CustomerAddressEntity> customerAddressEntity = customerAddressDao.getAddressByCustomer(customerAuth.getCustomer());


        return customerAddressEntity;

    }

    public String deleteAddress(String addressId, String authorization) throws AuthorizationFailedException, AddressNotFoundException {

        //validate user
        CustomerAuthEntity customerAuthTokenEntity = customerDao.checkAuthToken(authorization);
        AddressEntity addressEntity = addressDao.getAddressByUUID(addressId);
        CustomerAddressEntity customerAddressEntity = customerAddressDao.getSingleAddress(addressEntity);

        if (customerAuthTokenEntity.equals(null)) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }
        final ZonedDateTime customerSignOut = customerAuthTokenEntity.getLogoutAt();

        if (customerSignOut != null && customerAuthTokenEntity != null) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");
        }
        final ZonedDateTime customerSessionExpire = customerAuthTokenEntity.getExpiresAt();
        ZonedDateTime currentTime = ZonedDateTime.now(ZoneId.systemDefault());
        if (customerSessionExpire.compareTo(currentTime) < 0) {
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }
        if (addressEntity.getUuid().length() == 0){
            throw new AddressNotFoundException("ANF-005", "Address id can not be empty");
        }
        if (addressEntity == null) {
            throw new AddressNotFoundException("ANF-003", "No address by this id");
        }
        if (customerAuthTokenEntity.getCustomer().getId().equals(customerAddressEntity.getCustomer().getId())) {
            return addressDao.deleteAddress(addressId);
        } else {
            throw new AuthorizationFailedException("ATHR-004", "You are not authorized to view/update/delete any one else's address");
        }

    }

    public AddressEntity getAddressById(String id) {
        return addressDao.getAddressByUUID(id);
    }

}
