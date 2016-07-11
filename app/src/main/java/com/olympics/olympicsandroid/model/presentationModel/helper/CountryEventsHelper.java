package com.olympics.olympicsandroid.model.presentationModel.helper;

import com.olympics.olympicsandroid.model.CountryProfileEvents;
import com.olympics.olympicsandroid.model.OlympicAthlete;
import com.olympics.olympicsandroid.model.OlympicDiscipline;
import com.olympics.olympicsandroid.model.OlympicEvent;
import com.olympics.olympicsandroid.model.OlympicSchedule;
import com.olympics.olympicsandroid.model.OlympicSport;
import com.olympics.olympicsandroid.model.OlympicUnit;
import com.olympics.olympicsandroid.model.presentationModel.Athlete;
import com.olympics.olympicsandroid.model.presentationModel.CountryEventUnitModel;
import com.olympics.olympicsandroid.model.presentationModel.DateSportsModel;
import com.olympics.olympicsandroid.model.presentationModel.EventUnitModel;
import com.olympics.olympicsandroid.utility.DateUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This helper maps CountryProfileEvents and OlympicSchedule models into CountryEventUnitModel
 *
 * Created by sarnab.poddar on 7/10/16.
 */
public class CountryEventsHelper {

    private CountryProfileEvents countryProfileEvents;
    private OlympicSchedule olympicSchedule;



    public CountryEventsHelper(CountryProfileEvents countryProfileEvents, OlympicSchedule
            olympicSchedule) {
        // countryProfileEvents will have all events for a given country
        this.countryProfileEvents = countryProfileEvents;
        // olympicSchedule will hold all schedules for all countries
        this.olympicSchedule = olympicSchedule;
    }

    /*
     * The aim of this method is to create a CountryEventUnitModel = which will have the list of
     * unique athletes participating for that country.
     *  and also events #DateSportsModel sorted by date.
     */
    public CountryEventUnitModel createCountryEventUnitModel() {

        CountryEventUnitModel countryEventUnitModel = new CountryEventUnitModel();

        countryEventUnitModel.setCountryAlias(countryProfileEvents.getOrganization().getAlias());
        countryEventUnitModel.setCountryName(countryProfileEvents.getOrganization()
                .getDescription());
        countryEventUnitModel.setCountryID(countryProfileEvents.getOrganization().getId());

        countryEventUnitModel.setAthleteList(getAthleteListForCountry());

        // initialize mapping and then update with data
        countryEventUnitModel.initializeEmptyDateSportsMapping();
        updateDateSportsMapping(countryEventUnitModel.getDatesCountryMapping());

        return countryEventUnitModel;
    }

    /**
     * This method populates List of Athlete from CountryProfileEvents
     * @return
     */
    private List<Athlete> getAthleteListForCountry() {

        Set<Athlete> athleteList = new HashSet<>();
        for (OlympicEvent everyEvent : countryProfileEvents.getOrganization().getEvents()) {
            if (everyEvent == null || everyEvent.getParticipants() == null || everyEvent
                    .getParticipants().isEmpty()) {
                continue;
            }

            List<OlympicAthlete> participantList = everyEvent.getParticipants();
            for (OlympicAthlete participant : participantList) {
                if (participant == null) {
                    continue;
                }
                Athlete athlete = new Athlete();
                athlete.setAthleteName(participant.getPrint_name());
                athlete.setAthleteGender(participant.getGender());
            }

        }
        return new ArrayList<>(athleteList);
    }

    /**
     *
     * @return map of <Unit_start_date,
     */
    private void updateDateSportsMapping(Map<String, DateSportsModel> dateSportsMapping) {

        Map<String, OlympicEvent> allEventsMap = populateEventMapFromAllEventsSchedule();

        // Get each event that selected country is participating in
        for (OlympicEvent participatingEvent : countryProfileEvents.getOrganization().getEvents()) {
            if (participatingEvent == null) {
                continue;
            }

            OlympicEvent scheduledParticipatingEvent = allEventsMap.get(participatingEvent.getId());
            //Get all the units for the given event.
            if (scheduledParticipatingEvent == null || scheduledParticipatingEvent.getUnits() == null || scheduledParticipatingEvent
                    .getUnits().isEmpty()) {
                continue;
            }

            for (OlympicUnit olympicEventUnit : scheduledParticipatingEvent.getUnits()) {
                if (olympicEventUnit == null || olympicEventUnit.getStart_date() == null) {
                    continue;
                }

                String unitStartDate = DateUtils.getDateTimeInMillis(olympicEventUnit
                        .getStart_date());
                DateSportsModel dateSportsModel = dateSportsMapping.get(unitStartDate);

                if (dateSportsModel == null) {
                    dateSportsModel = new DateSportsModel();
                    dateSportsModel.setDateString(Long.parseLong(unitStartDate));
                    dateSportsMapping.put(unitStartDate, dateSportsModel);
                    continue;
                }

                //Set sports
                Map<String, DateSportsModel.SportsEventsUnits> sportsEventsUnits =
                            dateSportsModel.getAllSportsForDate();

                if (sportsEventsUnits == null) {
                    sportsEventsUnits = new HashMap<>();
                    dateSportsModel.setAllSportsForDate(sportsEventsUnits);
                }

                DateSportsModel.SportsEventsUnits sportsEventsUnit = sportsEventsUnits.get
                        (participatingEvent.getSport().getDescription());

                if (sportsEventsUnit == null) {
                    sportsEventsUnit = new DateSportsModel.SportsEventsUnits();
                    sportsEventsUnit.setSportsTitle(participatingEvent.getSport().getDescription());
                    sportsEventsUnits.put(participatingEvent.getSport().getDescription(), sportsEventsUnit);
                }

                List<EventUnitModel> eventUnitModelList = sportsEventsUnit.getEventUnits();
                if (eventUnitModelList == null) {
                    eventUnitModelList = new ArrayList<>();
                    sportsEventsUnit.setEventUnits(eventUnitModelList);
                }

                eventUnitModelList.add(populateEventUnitData(olympicEventUnit, participatingEvent));
            }
        }
    }

    /**
     * Populates map of <EventID, Event> from schedule API.
     * This will hold all events for all countries
     * @return
     */
    private Map<String, OlympicEvent> populateEventMapFromAllEventsSchedule() {

        Map<String, OlympicEvent> eventFromSchedule = new HashMap<>();

        // Populate all the events from Schedule list
        for (OlympicSport scheduledSports : olympicSchedule.getSeasonSchedule().getSports()) {
            for (OlympicDiscipline olympicDiscipline : scheduledSports.getDisciplines()) {
                for (OlympicEvent olympicEvent : olympicDiscipline.getEvents()) {
                    eventFromSchedule.put(olympicEvent.getId(), olympicEvent);
                }
            }
        }
        return eventFromSchedule;
    }

    private EventUnitModel populateEventUnitData(OlympicUnit olympicUnit, OlympicEvent
            olympicEvent) {

        EventUnitModel eventUnitModel = new EventUnitModel();
        eventUnitModel.setEventID(olympicUnit.getId());
        eventUnitModel.setUnitName(olympicUnit.getName());
        eventUnitModel.setEventGender(olympicEvent.getGender());
        //eventUnitModel.setEventType(olympicUnit.getType());
        // eventUnitModel.setUnitStatus(olympicUnit.getStatus());
        eventUnitModel.setUnitVenue(olympicUnit.getVenue().getName());
        // eventUnitModel.setUnitMedalType(olympicUnit.getPhase

        //eventUnitModel.setEventStartTime(DateUtils.prepareDate
        //        (olympicUnit.getStart_date()));
        return eventUnitModel;
    }

}
