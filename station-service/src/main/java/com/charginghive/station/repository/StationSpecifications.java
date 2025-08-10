package com.charginghive.station.repository;

import com.charginghive.station.model.Station;
import com.charginghive.station.model.StationPort;
import jakarta.persistence.criteria.Join;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

// added: dynamic specifications for filtering Stations
@NoArgsConstructor
public final class StationSpecifications {


    // added: filter by approval flag
    public static Specification<Station> hasApproval(Boolean approved) {
        return (root, query, cb) -> approved == null ? null : cb.equal(root.get("isApproved"), approved);
    }

    // added: filter by city (case-insensitive)
    public static Specification<Station> hasCity(String city) {
        return (root, query, cb) -> city == null ? null : cb.equal(cb.lower(root.get("city")), city.toLowerCase());
    }

    // added: filter by state (case-insensitive)
    public static Specification<Station> hasState(String state) {
        return (root, query, cb) -> state == null ? null : cb.equal(cb.lower(root.get("state")), state.toLowerCase());
    }

    // added: filter by ownerId
    public static Specification<Station> hasOwnerId(Long ownerId) {
        return (root, query, cb) -> ownerId == null ? null : cb.equal(root.get("ownerId"), ownerId);
    }

    // added: fuzzy name contains (case-insensitive)
    public static Specification<Station> nameContains(String name) {
        return (root, query, cb) -> name == null ? null : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    // added: price range filter (min or max may be null)
    public static Specification<Station> priceBetween(Double min, Double max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return null;
            }
            // Join the Station entity with its StationPort collection
            Join<Station, StationPort> ports = root.join("ports");
            query.distinct(true); // Ensure we don't get duplicate stations

            if (min != null && max != null) {
                return cb.between(ports.get("pricePerHour"), min, max);
            }
            if (min != null) {
                return cb.greaterThanOrEqualTo(ports.get("pricePerHour"), min);
            }
            return cb.lessThanOrEqualTo(ports.get("pricePerHour"), max);
        };
    }

    // added: latitude range
    public static Specification<Station> latitudeBetween(Double min, Double max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null) return cb.between(root.get("latitude"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("latitude"), min);
            return cb.lessThanOrEqualTo(root.get("latitude"), max);
        };
    }

    // added: longitude range
    public static Specification<Station> longitudeBetween(Double min, Double max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min != null && max != null) return cb.between(root.get("longitude"), min, max);
            if (min != null) return cb.greaterThanOrEqualTo(root.get("longitude"), min);
            return cb.lessThanOrEqualTo(root.get("longitude"), max);
        };
    }
}
