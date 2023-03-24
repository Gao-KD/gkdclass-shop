package org.example.biz;

import lombok.extern.slf4j.Slf4j;
import org.example.UserApplication;
import org.example.model.AddressDO;
import org.example.service.AddressService;
import org.example.utils.JsonData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RequestParam;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserApplication.class)
@Slf4j
public class AddressTest {
    @Autowired
    private AddressService addressService;

    @Test
    public void testAddressDetail(){
        AddressDO addressDO = addressService.detail(2L);
        log.info(String.valueOf(addressDO));
    }
}
