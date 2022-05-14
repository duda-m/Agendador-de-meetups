package com.bootcamp.microservicemeetup.controller;


import com.bootcamp.microservicemeetup.controller.resources.MeetupController;
import com.bootcamp.microservicemeetup.exception.BusinessException;
import com.bootcamp.microservicemeetup.controller.dto.MeetupDTO;
import com.bootcamp.microservicemeetup.model.entity.Meetup;
import com.bootcamp.microservicemeetup.model.entity.Registration;
import com.bootcamp.microservicemeetup.service.MeetupService;
import com.bootcamp.microservicemeetup.service.RegistrationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.google.common.eventbus.DeadEvent;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = {MeetupController.class})
@AutoConfigureMockMvc
public class MeetupControllerTest {

    static final String MEETUP_API = "/api/meetups";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private MeetupService meetupService;

    private Registration createNewUserRegistration() {
        return Registration.builder().id(11).name("Duda").registration("123").build();
    }

    private MeetupDTO createNewMeetup() {
        return MeetupDTO.builder().id(1).registrationAttribute("123").event("Womakers JAVA bootcamp").build();
    }


    @Test
    @DisplayName("Should register on a meetup")
    public void createMeetupTest() throws Exception {

        // quando enviar uma requisicao pra esse registration precisa ser encontrado um valor que tem esse usuario
        MeetupDTO dto = MeetupDTO.builder().registrationAttribute("123").event("Womakerscode Java").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        Registration registration = createNewUserRegistration();

        BDDMockito.given(registrationService.getRegistrationByRegistrationAttribute("123")).
                willReturn(Optional.of(registration));

        Meetup meetup = Meetup.builder().id(11).event("Womakerscode Java").registration(registration).meetupDate("10/10/2022").build();

        BDDMockito.given(meetupService.save(Mockito.any(Meetup.class))).willReturn(meetup);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(MEETUP_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        // Aqui retorna o id do registro no meetup
        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(content().string("11"));

    }


    @Test
    @DisplayName("Should return error when try to register a meetup nonexistent")
    public void invalidRegistrationCreateMeetupTest() throws Exception {

        MeetupDTO dto = MeetupDTO.builder().registrationAttribute("123").event("Womakerscode Java").build();
        String json = new ObjectMapper().writeValueAsString(dto);

        BDDMockito.given(registrationService.getRegistrationByRegistrationAttribute("123")).
                willReturn(Optional.empty());


        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(MEETUP_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

    }



    @Test
    @DisplayName("Should return error when try to register a registration already register on a meetup")
    public void  meetupRegistrationErrorOnCreateMeetupTest() throws Exception {

        MeetupDTO dto = MeetupDTO.builder().registrationAttribute("123").event("Womakerscode Java").build();
        String json = new ObjectMapper().writeValueAsString(dto);


        Registration registration = Registration.builder().id(11).name("Duda").registration("123").build();
        BDDMockito.given(registrationService.getRegistrationByRegistrationAttribute("123"))
                .willReturn(Optional.of(registration));

        // procura na base se ja tem algum registration pra esse meetup
        BDDMockito.given(meetupService.save(Mockito.any(Meetup.class))).willThrow(new BusinessException("Meetup already enrolled"));


        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .post(MEETUP_API)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should delete the meetup")
    public void deleteMeetupTest() throws Exception {

        BDDMockito.given(meetupService.getMeetupById(anyInt())).willReturn(Optional.of(Meetup.builder().id(11).build()));

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .delete(MEETUP_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should get meetup information")
    public void getMeetupInformationTest() throws Exception {

        Integer id = 101;

        Meetup meetup = Meetup.builder()
                .id(id)
                .event(createNewMeetup().getEvent())
                .registrationAttribute(createNewMeetup().getRegistrationAttribute())
                .build();

        BDDMockito.given(meetupService.getMeetupById(id)).willReturn(Optional.of(meetup));

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get(MEETUP_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        // assert
        mockMvc
                .perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("event").value(createNewMeetup().getEvent()))
                .andExpect(jsonPath("registrationAttribute").value(createNewMeetup().getRegistrationAttribute()));
    }



    @Test
    @DisplayName("Should return NOT FOUND when the meetup doesn't exists")
    public void meetupNotFoundTest() throws Exception {

        BDDMockito.given(meetupService.getMeetupById(anyInt())).willReturn(Optional.empty());

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get(MEETUP_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isNotFound());
    }


    @Test
    @DisplayName("Should update the meetup info")
    public void updateMeetupTest() throws Exception {

        Integer id = 101;
        String json = new ObjectMapper().writeValueAsString(createNewMeetup());

        Meetup updatingMeetup =
                Meetup.builder()
                        .id(id)
                        .event("Bootcamp Java")
                        .registrationAttribute("300")
                        .build();

        BDDMockito.given(meetupService.getMeetupById(anyInt()))
                .willReturn(Optional.of(updatingMeetup));

        Meetup updatedMeetup =
                Meetup.builder()
                        .id(id)
                        .event("Womakers JAVA bootcamp")
                        .registrationAttribute("300")
                        .build();

        BDDMockito.given(meetupService
                        .update(updatingMeetup))
                .willReturn(updatedMeetup);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .put(MEETUP_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("event").value(createNewMeetup().getEvent()))
                .andExpect(jsonPath("registrationAttribute").value("300"));
    }

}




