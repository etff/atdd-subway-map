package nextstep.subway.line.domain;

import nextstep.subway.station.application.StationDuplicateException;
import nextstep.subway.station.application.StationNotFoundException;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Embeddable
public class LineStations {

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "line_id", foreignKey = @ForeignKey(name = "fk_line_station_to_line"))
    private List<LineStation> lineStations = new ArrayList<>();

    public void appendStation(LineStation lineStation) {
        if (lineStation.getStationId() == null) {
            throw new StationNotFoundException();
        }

        if (lineStations.stream().anyMatch(it -> it.isSame(lineStation))) {
            throw new StationDuplicateException();
        }

        lineStations.stream()
                .filter(it -> it.getPreStationId() == lineStation.getStationId())
                .findAny()
                .ifPresent(it -> it.updatePreStation(lineStation.getStationId()));

        lineStations.add(lineStation);
    }

    public void removeStation(Long stationId) {
        LineStation lineStation = lineStations.stream()
                .filter(it -> it.getStationId() == stationId)
                .findFirst()
                .orElseThrow(StationNotFoundException::new);

        lineStations.stream()
                .filter(it -> it.getPreStationId() == lineStation.getStationId())
                .findAny()
                .ifPresent(it -> it.updatePreStation(lineStation.getPreStationId()));

        lineStations.remove(lineStation);
    }

    public List<LineStation> getOrderLineStations() {
        Optional<LineStation> preLineStation = lineStations.stream()
                .filter(it -> it.getPreStationId() == null)
                .findFirst();

        List<LineStation> result = new ArrayList<>();
        while (preLineStation.isPresent()) {
            LineStation preStationId = preLineStation.get();
            result.add(preStationId);
            preLineStation = lineStations.stream()
                    .filter(it -> it.getPreStationId() == preStationId.getStationId())
                    .findFirst();
        }
        return result;
    }
}
