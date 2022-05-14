package com.bootcamp.microservicemeetup.service;


import com.bootcamp.microservicemeetup.controller.dto.MeetupFilterDTO;
import com.bootcamp.microservicemeetup.model.entity.Meetup;
import com.bootcamp.microservicemeetup.model.entity.Registration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
public interface MeetupService {

    Meetup save(Meetup meetup);

    Optional<Meetup> getMeetupById(Integer id);

    void delete(Meetup meetup);

    Meetup update(Meetup loan);

    Page<Meetup> find(MeetupFilterDTO filterDTO, Pageable pageable);

    Page<Meetup> getRegistrationsByMeetup(Registration registration, Pageable pageable);

}
