package com.bootcamp.microservicemeetup.repository;


import com.bootcamp.microservicemeetup.model.entity.Meetup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest
public class MeetupRepositoryTest {

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    MeetupRepository meetupRepository;

    private Meetup createNewMeetup(String registrationAttribute) {
        return Meetup.builder().event("Bootcamp Java").meetupDate("12/06/2022").registrationAttribute(registrationAttribute).build();
    }

    @Test
    @DisplayName("Should return true when exists an registration attribute already created")
    public void returnTrueWhenRegistrationAttributeExistsTest() {

        String registrationAttribute = "200";

        Meetup reg = createNewMeetup(registrationAttribute);
        entityManager.persist(reg);

        boolean exist = meetupRepository.existsByEvent(reg.getEvent());

        assertThat(exist).isTrue();
    }

    @Test
    @DisplayName("Should return false when doesn't exists an registration attribute with a registration already created")
    public void returnFalseWhenRegistrationAttributeDoesntExistsTest() {

        String registrationAttribute = "123";

        boolean exist = meetupRepository.existsByEvent(registrationAttribute);

        assertThat(exist).isFalse();
    }

    @Test
    @DisplayName("Should get an meetup by id")
    public void findByIdTest() {

        Meetup registration_attribute = createNewMeetup("111");
        entityManager.persist(registration_attribute);

        Optional<Meetup> foundRegistrationAttribute = meetupRepository.findById(registration_attribute.getId());

        assertThat(foundRegistrationAttribute.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Should save an meetup")
    public void saveMeetupTest() {

        Meetup registrationAttribute = createNewMeetup("111");

        Meetup savedRegistrationAttribute = meetupRepository.save(registrationAttribute);

        assertThat(savedRegistrationAttribute.getId()).isNotNull();
    }

    @Test
    @DisplayName("Should delete a meetup from the base")
    public void deleteMeetupTest() {

        Meetup registrationAttribute = createNewMeetup("333");
        entityManager.persist(registrationAttribute);

        Meetup foundMeetup = entityManager.find(Meetup.class, registrationAttribute.getId());

        meetupRepository.delete(foundMeetup);

        Meetup deleteRegistrationAttribute = entityManager.find(Meetup.class, registrationAttribute.getId());

        assertThat(deleteRegistrationAttribute).isNull();
    }


}
