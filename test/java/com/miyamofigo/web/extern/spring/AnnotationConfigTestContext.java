package com.miyamofigo.web.extern.spring;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;

public class AnnotationConfigTestContext {

	@Configuration
	public static class AutowiredMethodConfig {

		@Bean
		public TestBean testBean(Colour colour, List<Colour> colours) {
			return new TestBean(colour.toString() + "-" + colours.get(0).toString());
		}
	}	

	@Configuration
	public static class ColorConfig {

		@Bean
		public Colour colour() {
			return Colour.RED;
		}
	}

	@Configuration
	public static class OptionalAutowiredMethodConfig {

		@Bean
		public TestBean testBean(Optional<Colour> colour, Optional<List<Colour>> colours) {
			if (!colour.isPresent() && !colours.isPresent()) {
				return new TestBean("");
			}
			else {
				return new TestBean(colour.get().toString() + "-" + colours.get().get(0).toString());
			}
		}
	}
}
