package nextstep.subway.line.application;

import nextstep.subway.common.domain.ByLineCalculator;
import nextstep.subway.common.domain.FareCaculator;
import nextstep.subway.common.domain.SubwayFare;
import nextstep.subway.common.domain.SurchargeByLine;
import nextstep.subway.line.domain.Line;
import nextstep.subway.line.domain.LineRepository;
import nextstep.subway.line.domain.Section;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.line.dto.SectionRequest;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Station;
import nextstep.subway.station.dto.StationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LineService {
    private LineRepository lineRepository;
    private StationService stationService;

    public LineService(LineRepository lineRepository, StationService stationService) {
        this.lineRepository = lineRepository;
        this.stationService = stationService;
    }


    public LineResponse saveLine(LineRequest request) {
        Station upStation = stationService.findStationById(request.getUpStationId());
        Station downStation = stationService.findStationById(request.getDownStationId());
        Line persistLine = lineRepository.save(
                new Line(request.getName()
                        , request.getColor()
                        , upStation
                        , downStation
                        , request.getDistance()
                        , calculateByLine(request.getName())));

        return LineResponse.of(persistLine, getStationResponses(persistLine));
    }

    private BigDecimal calculateByLine(String name) {
        FareCaculator lineCalculator = new ByLineCalculator();
        return lineCalculator.calculate(SubwayFare.ZERO, name);
    }

    @Transactional(readOnly = true)
    public List<LineResponse> findLines() {
        List<Line> persistLines = findAllLine();
        return LineResponse.listOf(persistLines);
    }

    public List<Line> findAllLine() {
        return lineRepository.findAll();
    }

    public Line findLineById(Long id) {
        return lineRepository
                .findById(id)
                .orElseThrow(RuntimeException::new);
    }

    @Transactional(readOnly = true)
    public LineResponse findLineResponseById(Long id) {
        Line persistLine = findLineById(id);
        return LineResponse.of(persistLine, getStationResponses(persistLine));
    }

    public void updateLine(Long id, LineRequest lineUpdateRequest) {
        Line persistLine = lineRepository
                .findById(id)
                .orElseThrow(RuntimeException::new);
        persistLine.update(new Line(lineUpdateRequest.getName(), lineUpdateRequest.getColor()));
    }

    public void deleteLineById(Long id) {
        lineRepository.deleteById(id);
    }

    public void addLineStation(Long lineId, SectionRequest request) {
        Line line = findLineById(lineId);
        Station upStation = stationService.findStationById(request.getUpStationId());
        Station downStation = stationService.findStationById(request.getDownStationId());
        line.addStation(upStation, downStation, request.getDistance());
    }

    public void removeLineStation(Long lineId, Long stationId) {
        Line line = findLineById(lineId);
        Station station = stationService.findStationById(stationId);
        line.removeStation(station);
    }

    private List<StationResponse> getStationResponses(Line line) {
        return line.getStations().stream()
                .map(station -> StationResponse.of(station))
                .collect(Collectors.toList());
    }

    public List<Section> findAllLineSectionList() {
        List<Line> lines = findAllLine();
        return lines.stream()
                .flatMap(line -> line.getSections().values().stream())
                .collect(Collectors.toList());
    }
}
