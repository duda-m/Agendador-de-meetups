package com.bootcamp.microservicemeetup.service.impl;


import com.bootcamp.microservicemeetup.controller.dto.MeetupFilterDTO;
import com.bootcamp.microservicemeetup.exception.BusinessException;
import com.bootcamp.microservicemeetup.model.entity.Meetup;
import com.bootcamp.microservicemeetup.model.entity.Registration;
import com.bootcamp.microservicemeetup.repository.MeetupRepository;
import com.bootcamp.microservicemeetup.service.MeetupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MeetupServiceImpl implements MeetupService {


    private MeetupRepository repository;

    public MeetupServiceImpl(MeetupRepository repository) {
        this.repository = repository;
    }

    @Override
    public Meetup save(Meetup meetup) {

        if (repository.existsByEvent(meetup.getEvent())) {
            throw new BusinessException("Meetup already created");
        }
        return repository.save(meetup);
    }

    @Override
    public Optional<Meetup> getMeetupById(Integer id) {
        return repository.findById(id);
    }

   @Override
    public void delete(Meetup meetup) {
        if (meetup == null || meetup.getId() == null) {
            throw new IllegalArgumentException("Meetup id cannot be null");
        }
        this.repository.delete(meetup);
    }

    @Override
    public Meetup update(Meetup meetup) {
        if (meetup == null || meetup.getId() == null) {
            throw new IllegalArgumentException("Meetup id cannot be null");
        }
        return this.repository.save(meetup);
    }

    @Override
    public Page<Meetup> find(MeetupFilterDTO filterDTO, Pageable pageable) {

        if (filterDTO.getRegistration() != null && filterDTO.getEvent() != null) {
            return repository.findByRegistrationOnMeetup(filterDTO.getRegistration(), filterDTO.getEvent(), pageable);
        }
        return repository.findAll(pageable);

    }

    @Override
    public Page<Meetup> getRegistrationsByMeetup(Registration registration, Pageable pageable) {
        return repository.findByRegistration(registration, pageable);
    }


}

