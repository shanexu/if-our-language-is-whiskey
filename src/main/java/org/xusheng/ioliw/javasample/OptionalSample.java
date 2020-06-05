package org.xusheng.ioliw.javasample;

import java.util.Arrays;
import java.util.Optional;

public class OptionalSample {

    public static Sheep father(Sheep sheep) {
        throw new UnsupportedOperationException();
    }

    public static Sheep mother(Sheep sheep) {
        throw new UnsupportedOperationException();
    }

    public static Optional<Sheep> fatherOpt(Sheep sheep) {
        return Optional.ofNullable(father(sheep));
    }

    public static Optional<Sheep> motherOpt(Sheep sheep) {
        return Optional.ofNullable(mother(sheep));
    }

    public static Sheep maternalGrandfather(Sheep s) {
        Sheep m = mother(s);
        if (m != null) {
            return father(m);
        }
        return null;
    }

    public static Optional<Sheep> maternalGrandfatherOpt(Sheep s) {
        return motherOpt(s).flatMap(OptionalSample::fatherOpt);
    }

    public static Sheep mothersPaternalGrandfather(Sheep s) {
        Sheep m = mother(s);
        if (m != null) {
            Sheep f = father(m);
            if (f != null) {
                return father(f);
            }
        }
        return null;
    }

    // flatMap === bind === Optional<T> >>= Function<T, Optional<K>>
    public static Optional<Sheep> mothersPaternalGrandfatherOpt(Sheep s) {
        return motherOpt(s)
            .flatMap(OptionalSample::fatherOpt)
            .flatMap(OptionalSample::fatherOpt);
    }

    public static Optional<Sheep> mothersFathersMothersFathersMotherOpt(Sheep s) {
        return motherOpt(s)
            .flatMap(OptionalSample::fatherOpt)
            .flatMap(OptionalSample::motherOpt)
            .flatMap(OptionalSample::fatherOpt)
            .flatMap(OptionalSample::motherOpt);
    }
}

class Sheep {}
