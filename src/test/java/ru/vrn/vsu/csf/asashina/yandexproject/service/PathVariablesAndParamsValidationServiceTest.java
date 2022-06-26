package ru.vrn.vsu.csf.asashina.yandexproject.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vrn.vsu.csf.asashina.yandexproject.exception.validation.DateFormatException;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class PathVariablesAndParamsValidationServiceTest {

    @InjectMocks
    private PathVariablesAndParamsValidationService pathVariablesAndParamsValidationService;

    @Test
    void validatesId() {
        // given
        var id = "96c9915e-3c09-430f-a314-7774697eeab5";

        // when
        UUID result = pathVariablesAndParamsValidationService.validateId(id);

        // then
        assertThat(result).isEqualTo(UUID.fromString(id));
    }

    @Test
    void validateIdThrowsExceptionForInvalidId() {
        // given
        var id = "96c9915e-3c09-430f-7774697eeab5";

        // when, then
        assertThatThrownBy(() -> pathVariablesAndParamsValidationService.validateId(id))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validatesDate() {
        // given
        var date = "2022-02-03T15:00:00.000Z";

        // when
        Instant result = pathVariablesAndParamsValidationService.validateDate(date);

        // then
        assertThat(result).isEqualTo(Instant.parse(date));
    }

    @Test
    void validateDateThrowExceptionForWrongDateFormat() {
        // given
        var date = "2022-02-03 15:00:00";

        // when, then
        assertThatThrownBy(() -> pathVariablesAndParamsValidationService.validateDate(date))
                .isInstanceOf(DateFormatException.class);
    }

    @Test
    void validateDateThrowExceptionForInvalidDate() {
        // given
        var date = "abc";

        // when, then
        assertThatThrownBy(() -> pathVariablesAndParamsValidationService.validateDate(date))
                .isInstanceOf(DateFormatException.class);
    }
}