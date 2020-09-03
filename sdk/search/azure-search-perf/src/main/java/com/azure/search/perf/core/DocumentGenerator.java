// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.perf.core;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates Search documents.
 */
public final class DocumentGenerator {
    /**
     * Creates a number of documents of the given size.
     *
     * @param count Number of documents.
     * @param documentSize Size of the documents.
     * @return A list of documents.
     */
    public static List<Hotel> generateHotels(int count, DocumentSize documentSize) {
        List<Hotel> hotels = new ArrayList<>(count * 2);

        for (int i = 0; i < count; i++) {
            hotels.add(createHotel(i, documentSize));
        }

        return hotels;
    }

    private static Hotel createHotel(int id, DocumentSize documentSize) {
        Hotel hotel = new Hotel();

        hotel.hotelId = String.valueOf(id);

        if (documentSize == DocumentSize.SMALL) {
            hotel.hotelName = "Secret Point Motel";
            hotel.description = "The hotel is ideally located on the main commercial artery of the city in the heart "
                + "of New York. A few minutes away is Time's Square and the historic centre of the city, as well "
                + "as other places of interest that make New York one of America's most attractive and cosmopolitan "
                + "cities.";
            hotel.descriptionFr = "L'hôtel est idéalement situé sur la principale artère commerciale de la ville "
                + "en plein cœur de New York. A quelques minutes se trouve la place du temps et le centre historique "
                + "de la ville, ainsi que d'autres lieux d'intérêt qui font de New York l'une des villes les plus "
                + "attractives et cosmopolites de l'Amérique.";
            hotel.category = "Boutique";
            hotel.tags = new String[]{"pool", "air conditioning", "concierge"};
            hotel.parkingIncluded = false;
            hotel.lastRenovationDate = OffsetDateTime.of(1970, 1, 18, 0, 0, 0, 0, ZoneOffset.UTC);
            hotel.rating = 3.6D;

            Address address = new Address();
            address.streetAddress = "677 5th Ave";
            address.city = "New York";
            address.stateProvince = "NY";
            address.postalCode = "10022";
            address.country = "USA";

            hotel.address = address;
        } else {
            hotel.hotelName = "Mount Rainier Lodge";
            hotel.description = "Mount Rainier Lodge is a historic lodge located in Ashford, Washington just a couple "
                + "of miles outside of Mount Rainier National Park. To complement your trip to the great outdoors, the "
                + "lodge offers queen rooms, suites, and standalone cabins with an outdoorsy theme. Amenities include "
                + "a continental breakfast, high speed wi-fi, a gift shop, basic cable, an outdoor jacuzzi and free "
                + "national park maps. With only a few hundred residents, Ashford has that quintessential outdoorsy "
                + "small town feel while offering restaurants, gas stations, and convenience stores to keep you well "
                + "supplied for your trip. Located to the east of the park, this lodge is just a few hours from both "
                + "Mount St. Helens and Mount Adams so you can explore much of what the pacific northwest has to "
                + "offer. If you choose to stay near Rainier, you’ll still get to take advantage of the tremendous "
                + "view. Whether you’re looking to summit Mount Rainier, hiking all of Wonderland Trail, hoping to "
                + "see a grizzly bear, or just exploring the park, Mount Rainier Lodge can be the perfect resting "
                + "place for your adventure. We hope to see you soon!";
            hotel.descriptionFr = "Mount Rainier Lodge est un lodge historique situé à Ashford, Washington à quelques "
                + "miles à l’extérieur du parc national du Mont Rainier. Pour compléter votre voyage en plein air, le "
                + "lodge propose des chambres reines, des suites et des cabines autonomes avec un thème extérieur. "
                + "Les commodités comprennent un petit déjeuner continental, wi-fi haute vitesse, une boutique de "
                + "cadeaux, un câble de base, un jacuzzi extérieur et des cartes gratuites du parc national. Avec "
                + "seulement quelques centaines de résidents, Ashford a cette quintessence en plein air petite ville "
                + "se sentent tout en offrant des restaurants, stations-service, et des dépanneurs pour vous garder "
                + "bien fourni pour votre voyage. Situé à l’est du parc, ce lodge est à quelques heures du mont St. "
                + "Helens et du mont Adams afin que vous puissiez explorer une grande partie de ce que le nord-ouest "
                + "du Pacifique a à offrir. Si vous choisissez de rester près de Rainier, vous aurez toujours la "
                + "chance de profiter de la vue imprenable. Que vous cherchiez au sommet du mont Rainier, que vous "
                + "randonnées sur l’ensemble du sentier Wonderland, que vous espériez voir un grizzli ou que vous "
                + "exploriez simplement le parc, le Mount Rainier Lodge peut être le lieu de repos idéal pour votre "
                + "aventure. Nous espérons vous voir bientôt!";
            hotel.category = "Lodge";
            hotel.tags = new String[]{
                "jacuzzi", "air conditioning", "gift shop", "basic cable", "continental breakfast", "free wi-fi",
                "national park", "cabin", "outdoors", "pacific northwest", "mountain"
            };
            hotel.parkingIncluded = true;
            hotel.lastRenovationDate = OffsetDateTime.of(1985, 3, 30, 0, 0, 0, 0, ZoneOffset.UTC);
            hotel.rating = 4.2D;

            Address address = new Address();
            address.streetAddress = "35708 Sr 706 E";
            address.city = "Ashford";
            address.stateProvince = "WA";
            address.postalCode = "98304";
            address.country = "USA";

            hotel.address = address;
        }

        return hotel;
    }
}
