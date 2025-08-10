package com.charginghive.station;

import com.charginghive.station.dto.CreateStationRequestDto;
import com.charginghive.station.model.Station;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class StationApplication {

	public static void main(String[] args) {
		SpringApplication.run(StationApplication.class, args);
	}

	@Bean
	ModelMapper modelMapper() {
		System.out.println("creating model mapper");
		ModelMapper mapper= new ModelMapper();
		//to transfer only properties matching by name
		mapper.getConfiguration()
				.setMatchingStrategy(MatchingStrategies.STRICT)
				//transfer not null props
				.setPropertyCondition(Conditions.isNotNull());

		// Skip automatic mapping for the 'ports' collection.
                // This will be handled manually in the service layer 
		mapper.typeMap(CreateStationRequestDto.class, Station.class)
				.addMappings(m -> m.skip(Station::setPorts));
		return mapper;
	}
}
