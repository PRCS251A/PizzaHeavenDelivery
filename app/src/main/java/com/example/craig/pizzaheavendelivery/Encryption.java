package com.example.craig.pizzaheavendelivery;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import pizzaheaven.models.Customer;
import pizzaheaven.models.Staff;

/**
 * Created by craig on 03/05/2017.
 */

public class Encryption {

    public static Object encrypt(Object obj){
        if (obj instanceof Customer) {
            Customer customer = (Customer)obj;
            String privateKey = customer.getPrivateKey();
            String firstName = customer.getFirstName();
            String surname = customer.getSurname();
            String phoneNumber = customer.getPhoneNumber();
            String deliveryLineOne = customer.getDeliveryLineOne();
            String deliveryLineTwo = customer.getDeliveryLineTwo();
            String deliveryCity = customer.getDeliveryCity();
            String deliveryCounty = customer.getDeliveryCounty();
            String deliveryPostCode = customer.getDeliveryPostCode();
            String email = customer.getEmail();
            String password = customer.getCustomerPassword();
            String salt = customer.getSalt();

            customer.setFirstName(encryption(privateKey, firstName));
            customer.setSurname(encryption(privateKey, surname));
            customer.setPhoneNumber(encryption(privateKey, phoneNumber));
            customer.setDeliveryLineOne(encryption(privateKey, deliveryLineOne));
            customer.setDeliveryLineTwo(encryption(privateKey, deliveryLineTwo));
            customer.setDeliveryCity(encryption(privateKey, deliveryCity));
            customer.setDeliveryCounty(encryption(privateKey, deliveryCounty));
            customer.setDeliveryPostCode(encryption(privateKey, deliveryPostCode));
            customer.setEmail(encryption(privateKey, email));
            customer.setCustomerPassword(encryption(privateKey, password));
            customer.setSalt(encryption(privateKey, salt));
            return customer;
        } else if (obj instanceof Staff) {
            Staff staff = (Staff)obj;
            String privateKey = staff.getPrivateKey();
            String firstName = staff.getFirstName();
            String surname = staff.getSurname();
            String phoneNumber = staff.getPhoneNumber();
            String addressLineOne = staff.getAddressLineOne();
            String addressLineTwo = staff.getAddressLineTwo();
            String city = staff.getCity();
            String county = staff.getCounty();
            String postCode = staff.getPostCode();
            String compPosition = staff.getCompanyPosition();
            String email = staff.getEmail();
            String password = staff.getPassword();
            String salt = staff.getSalt();
            String employed = staff.getEmployed();

            staff.setFirstName(encryption(privateKey, firstName));
            staff.setSurname(encryption(privateKey, surname));
            staff.setPhoneNumber(encryption(privateKey, phoneNumber));
            staff.setAddressLineOne(encryption(privateKey, addressLineOne));
            staff.setAddressLineTwo(encryption(privateKey, addressLineTwo));
            staff.setCity(encryption(privateKey, city));
            staff.setCounty(encryption(privateKey, county));
            staff.setPostCode(encryption(privateKey, postCode));
            staff.setEmail(encryption(privateKey, email));
            staff.setPassword(encryption(privateKey, password));
            staff.setCompanyPosition(encryption(privateKey, compPosition));
            staff.setSalt(encryption(privateKey, salt));
            staff.setEmployed(encryption(privateKey, employed));
            return staff;
        }
        return null;
    }

    public static Object decrypt(Object obj){
        if (obj instanceof Customer) {
            Customer customer = (Customer)obj;
            String privateKey = customer.getPrivateKey();
            String firstName = customer.getFirstName();
            String surname = customer.getSurname();
            String phoneNumber = customer.getPhoneNumber();
            String deliveryLineOne = customer.getDeliveryLineOne();
            String deliveryLineTwo = customer.getDeliveryLineTwo();
            String deliveryCity = customer.getDeliveryCity();
            String deliveryCounty = customer.getDeliveryCounty();
            String deliveryPostCode = customer.getDeliveryPostCode();
            String email = customer.getEmail();
            String password = customer.getCustomerPassword();
            String salt = customer.getSalt();

            customer.setFirstName(decryption(privateKey, firstName));
            customer.setSurname(decryption(privateKey, surname));
            customer.setPhoneNumber(decryption(privateKey, phoneNumber));
            customer.setDeliveryLineOne(decryption(privateKey, deliveryLineOne));
            customer.setDeliveryLineTwo(decryption(privateKey, deliveryLineTwo));
            customer.setDeliveryCity(decryption(privateKey, deliveryCity));
            customer.setDeliveryCounty(decryption(privateKey, deliveryCounty));
            customer.setDeliveryPostCode(decryption(privateKey, deliveryPostCode));
            customer.setEmail(decryption(privateKey, email));
            customer.setCustomerPassword(decryption(privateKey, password));
            customer.setSalt(decryption(privateKey, salt));
            return customer;
        } else if (obj instanceof Staff) {
            Staff staff = (Staff)obj;
            String privateKey = staff.getPrivateKey();
            String firstName = staff.getFirstName();
            String surname = staff.getSurname();
            String phoneNumber = staff.getPhoneNumber();
            String addressLineOne = staff.getAddressLineOne();
            String addressLineTwo = staff.getAddressLineTwo();
            String city = staff.getCity();
            String county = staff.getCounty();
            String postCode = staff.getPostCode();
            String compPosition = staff.getCompanyPosition();
            String email = staff.getEmail();
            String password = staff.getPassword();
            String salt = staff.getSalt();
            String employed = staff.getEmployed();

            staff.setFirstName(decryption(privateKey, firstName));
            staff.setSurname(decryption(privateKey, surname));
            staff.setPhoneNumber(decryption(privateKey, phoneNumber));
            staff.setAddressLineOne(decryption(privateKey, addressLineOne));
            staff.setAddressLineTwo(decryption(privateKey, addressLineTwo));
            staff.setCity(decryption(privateKey, city));
            staff.setCounty(decryption(privateKey, county));
            staff.setPostCode(decryption(privateKey, postCode));
            staff.setEmail(decryption(privateKey, email));
            staff.setPassword(decryption(privateKey, password));
            staff.setCompanyPosition(decryption(privateKey, compPosition));
            staff.setSalt(decryption(privateKey, salt));
            staff.setEmployed(decryption(privateKey, employed));
            return staff;
        }
        return null;
    }


    protected static String decryption(String priKey, String base64) {
        try {
            //128 bit key
            String pubKey = "PizzaHeavenCDJMZ";
            SecretKeySpec sKey = new SecretKeySpec(pubKey.getBytes("UTF-8"),
                    "AES");
            IvParameterSpec iv = new IvParameterSpec(priKey.getBytes("UTF-8"));

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, sKey, iv);
            byte[] original = cipher.doFinal(Base64.decode(base64, 0));

            return new String(original);
        } catch (Exception e) {

        }
        return null;
    }

    public static String encryption(String priKey, String value) {
        try {
            //128 bit key = 16x 8 bit characters
            String pubKey = "PizzaHeavenCDJMZ";
            SecretKeySpec sKey = new SecretKeySpec(pubKey.getBytes("UTF-8"),
                    "AES");
            IvParameterSpec iv = new IvParameterSpec(priKey.getBytes("UTF-8"));

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, sKey, iv);
            byte[] encrypted = cipher.doFinal(value.getBytes());

            return Base64.encodeToString(encrypted, 0);
        } catch (Exception e) {

        }
        return null;
    }

}
