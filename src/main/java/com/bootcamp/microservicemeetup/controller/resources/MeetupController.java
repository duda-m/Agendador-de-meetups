package com.bootcamp.microservicemeetup.controller.resources;

import com.bootcamp.microservicemeetup.controller.dto.MeetupDTO;
import com.bootcamp.microservicemeetup.controller.dto.MeetupFilterDTO;
import com.bootcamp.microservicemeetup.controller.dto.RegistrationDTO;
import com.bootcamp.microservicemeetup.model.entity.Meetup;
import com.bootcamp.microservicemeetup.model.entity.Registration;
import com.bootcamp.microservicemeetup.service.MeetupService;
import com.bootcamp.microservicemeetup.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/meetups")
@RequiredArgsConstructor
public class MeetupController {

    private final MeetupService meetupService;
    private final RegistrationService registrationService;
    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    private Integer create(@RequestBody MeetupDTO meetupDTO) {

        Registration registration = registrationService.getRegistrationByRegistrationAttribute(meetupDTO.getRegistrationAttribute())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        Meetup entity = Meetup.builder()
                .registration(registration)
                .event(meetupDTO.getEvent())
                .registrationAttribute(meetupDTO.getRegistrationAttribute())
                .meetupDate("10/06/2022")
                .build();

        entity = meetupService.save(entity);
        return entity.getId();
    }


    @GetMapping
    public Page<MeetupDTO> find(MeetupFilterDTO dto, Pageable pageRequest) {
        Page<Meetup> result = meetupService.find(dto, pageRequest);
        List<MeetupDTO> meetups = result
                .getContent()
                .stream()
                .map(entity -> {

                    Registration registration = entity.getRegistration();
                    RegistrationDTO registrationDTO = modelMapper.map(registration, RegistrationDTO.class);

                    MeetupDTO meetupDTO = modelMapper.map(entity, MeetupDTO.class);
                    meetupDTO.setRegistration(registrationDTO);
                    return meetupDTO;

                }).collect(Collectors.toList());
        return new PageImpl<MeetupDTO>(meetups, pageRequest, result.getTotalElements());
    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    public MeetupDTO get(@PathVariable Integer id) {

        return meetupService
                .getMeetupById(id)
                .map(meetup -> modelMapper.map(meetup, MeetupDTO.class))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }


    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByMeetupId(@PathVariable Integer id) {
        Meetup meetup = meetupService.getMeetupById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        meetupService.delete(meetup);
    }

    @PutMapping("{id}")
    public MeetupDTO update(@PathVariable Integer id, @RequestBody @Valid MeetupDTO meetupDTO) {
        return meetupService.getMeetupById(id).map(meetup -> {
            meetup.setRegistrationAttribute(meetupDTO.getRegistrationAttribute());
            meetup.setEvent(meetupDTO.getEvent());
            meetup = meetupService.update(meetup);
            return modelMapper.map(meetup, MeetupDTO.class);
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }



}