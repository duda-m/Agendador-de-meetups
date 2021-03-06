package com.bootcamp.microservicemeetup.service;

import com.bootcamp.microservicemeetup.exception.BusinessException;
import com.bootcamp.microservicemeetup.model.entity.Registration;
import com.bootcamp.microservicemeetup.repository.RegistrationRepository;
import com.bootcamp.microservicemeetup.service.impl.RegistrationServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class RegistrationServiceTest {

    RegistrationService registrationService;

    @MockBean
    RegistrationRepository registrationRepository;

    @BeforeEach
    public void setUp(){
        //service dependency
        this.registrationService = new RegistrationServiceImpl(registrationRepository);
    }

    @Test
    @DisplayName("Should have an registration")
    public void saveRegistration(){


        //cenario
        Registration registration = createValidRegistration();

        //execucao
        Mockito.when(registrationRepository.existsByRegistration(Mockito.anyString())).thenReturn(false);
        Mockito.when(registrationRepository.save(registration)).thenReturn(createValidRegistration());

        Registration savedRegistration = registrationService.save(registration);

        //assert
        assertThat(savedRegistration.getId()).isEqualTo(101);
        assertThat(savedRegistration.getName()).isEqualTo("Duda");
        assertThat(savedRegistration.getDateOfRegistration()).isEqualTo("29/04/22");
        assertThat(savedRegistration.getRegistration()).isEqualTo("001");

    }

    @Test
    @DisplayName("Should throw business error when try " +
            "to save a new registration with a registration duplicated")
    public void shouldNotSaveAsRegistrationDuplicated() {

        Registration registration = createValidRegistration();
        Mockito.when(registrationRepository.existsByRegistration(Mockito.any())).thenReturn(true);

        Throwable exception = Assertions.catchThrowable( () -> registrationService.save(registration));
        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Registration already created");

        //mockito will never save duplicated information
        Mockito.verify(registrationRepository, Mockito.never()).save(registration);
    }


    @Test
    @DisplayName("Should get an Registration by Id")
    public void getRegistrationByIdTest(){

        //cenario
        Integer id =11;
        Registration registration = createValidRegistration();
        registration.setId(id);
        Mockito.when(registrationRepository.findById(id)).thenReturn(Optional.of(registration));

        //execucao
        Optional<Registration> foundRegistration = registrationService.getRegistrationById(id);

        //asserts
        assertThat(foundRegistration.isPresent()).isTrue();
        assertThat(foundRegistration.get().getId()).isEqualTo(id);
        assertThat(foundRegistration.get().getName()).isEqualTo(registration.getName());
        assertThat(foundRegistration.get().getDateOfRegistration()).isEqualTo(registration.getDateOfRegistration());
        assertThat(foundRegistration.get().getRegistration()).isEqualTo(registration.getRegistration());
    }

    @Test
    @DisplayName("Should return empty when get an registration by id when doesn't exists")
    public void registrationNotFoundByIdTest() {

        Integer id = 11;
        Mockito.when(registrationRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Registration> registration  = registrationService.getRegistrationById(id);

        assertThat(registration.isPresent()).isFalse();
    }

    @Test
    @DisplayName("Should delete an registration")
    public void deleteRegistrationTest(){

        Registration registration = Registration.builder().id(11).build();

        assertDoesNotThrow(() -> registrationService.delete(registration));

        Mockito.verify(registrationRepository, Mockito.times(1)).delete(registration);
    }

    @Test
    @DisplayName("Should upadate an registration")
    public void updateRegistration(){

        //cenario
        Integer id = 11;
        Registration updatingRegistration = createValidRegistration();

        //execucao
        Registration updatedRegistration = createValidRegistration();
        updatedRegistration.setId(id);

        Mockito.when(registrationRepository.save(updatingRegistration)).thenReturn(updatedRegistration);
        Registration registration = registrationService.update(updatingRegistration);

        //asserts
        assertThat(registration.getId()).isEqualTo(updatedRegistration.getId());
        assertThat(registration.getName()).isEqualTo(updatedRegistration.getName());
        assertThat(registration.getDateOfRegistration()).isEqualTo(updatedRegistration.getDateOfRegistration());
        assertThat(registration.getRegistration()).isEqualTo(updatedRegistration.getRegistration());

    }

    //como se fosse um find all, vamos fazer uma busca atraves das propriedades do registration
    @Test
    @DisplayName("Should filter registration")
    public void findRegistrationTest() {

        // cenario
        Registration registration = createValidRegistration();
        PageRequest pageRequest = PageRequest.of(0,10);

        List<Registration> listRegistrations = Arrays.asList(registration);
        Page<Registration> page = new PageImpl<Registration>(Arrays.asList(registration),
                PageRequest.of(0,10), 1);

        // execucao
        Mockito.when(registrationRepository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        Page<Registration> result = registrationService.find(registration, pageRequest);

        // assercao
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(listRegistrations);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should get an Registration model by registration attribute")
    public void getRegistrationByRegistrationAtrb() {

        String registrationAttribute = "1234";

        Mockito.when(registrationRepository.findByRegistration(registrationAttribute))
                .thenReturn(Optional.of(Registration.builder().id(11).registration(registrationAttribute).build()));

        Optional<Registration> registration  = registrationService.getRegistrationByRegistrationAttribute(registrationAttribute);

        assertThat(registration.isPresent()).isTrue();
        assertThat(registration.get().getId()).isEqualTo(11);
        assertThat(registration.get().getRegistration()).isEqualTo(registrationAttribute);

        Mockito.verify(registrationRepository, Mockito.times(1)).findByRegistration(registrationAttribute);

    }

    private Registration createValidRegistration() {
        return Registration.builder()
                .id(101)
                .name("Duda")
                .dateOfRegistration("29/04/22")
                .registration("001")
                .build();
    }


}
