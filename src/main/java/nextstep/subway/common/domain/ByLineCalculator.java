package nextstep.subway.common.domain;

import java.math.BigDecimal;
import java.util.Arrays;

import static nextstep.subway.common.domain.SurchargeByLine.NORMAL;

public class ByLineCalculator implements FareCaculator<BigDecimal, String> {

    @Override
    public BigDecimal calculate(BigDecimal subwayFare, String linName) {
        BigDecimal surcharge = Arrays.stream(SurchargeByLine.values())
                .filter(surchargeByLine -> surchargeByLine.getName().equals(linName))
                .findFirst()
                .map(surchargeByLine -> surchargeByLine.charge())
                .orElse(NORMAL.charge());

        return surcharge;
    }
}
