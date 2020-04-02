package com.azure.storage.blob.changefeed

class BlobChangefeedTest extends APISpec {

//    def "basic sync implementation"() {
//        setup:
//        def cfClient = primaryBlobServiceClient.getBlobContainerClient('$blobchangefeed')
//
//        int numCFE = 0;
//        // Years - 1 page
//        for (def year : cfClient.listBlobsByHierarchy("idx/segments/")
//            .iterator()) {
//            System.out.println("YEAR " + year.getName())
//            // Segments - at most 2 pages
//            for (def segment : cfClient.listBlobs(new ListBlobsOptions().setPrefix(year.getName()), null).iterator()) {
//                // Each segment blob contains JSON indicating location of shards
//                System.out.println("SEGMENT " + segment.getName())
//                def segmentClient = cfClient.getBlobClient(segment.getName())
//                def os = new ByteArrayOutputStream()
//                segmentClient.download(os)
//
//                ObjectMapper objectMapper = new ObjectMapper();
//                JsonNode jsonNode = objectMapper.readTree(os.toString());
//
//                // Flux.merge each shard to round robin
//                // Shards
//                // Round robin
//                for (JsonNode shard : jsonNode.withArray("chunkFilePaths")) {
//                    System.out.println("SHARD " + shard.asText().substring('$blobchangefeed/'.length()))
//                    // Chunk
//                    for (def chunk : cfClient.listBlobsByHierarchy(shard.asText().substring('$blobchangefeed/'.length())).iterator()) {
//                        // Multiple events can be in here.
//                        System.out.println("CHUNK " + chunk.getName())
//                        def bc = cfClient.getBlobClient(chunk.getName())
//                        def is = bc.openInputStream()
//                        DataFileStream<GenericRecord> parsedStream = new DataFileStream<>(is, new GenericDatumReader<>())
//                        int num = 0
//                        while (parsedStream.hasNext()) {
//                            num++
//                            numCFE++
//                            parsedStream.next()
//                        }
//                        System.out.println("NUMBER OF CFE " + num)
//                    }
//                }
//            }
//        }
//        System.out.println("TOTAL NUMBER OF CFE " + numCFE)
//    }
//
//    def "get years"() {
//        setup:
//        def client = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient('$blobchangefeed')
//        // Years
//        def years = client.listBlobsByHierarchy("idx/segments/")
//
//        when:
//        def sv = StepVerifier.create(
//            years.map({ item -> item.getName() })
//        )
//        then:
//        sv.expectNext("idx/segments/1601/")
//            .expectNext("idx/segments/2019/")
//            .expectNext("idx/segments/2020/")
//            .expectComplete()
//            .verify();
//    }
//
//    def "get segments"() {
//        setup:
//        def client = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient('$blobchangefeed')
//        // Years
//        def segments = client.listBlobsByHierarchy("idx/segments/")
//            .concatMap({ year ->
//                System.out.println("YEAR : " + year.getName())
//                return client.listBlobs(new ListBlobsOptions().setPrefix(year.getName()))
//            })
//
//        when:
//        def sv = StepVerifier.create(
//            segments.map({ segment ->
//                System.out.println("SEGMENT: " + segment.getName())
//                segment.getName()
//            })
//        )
//        then:
//        sv.expectNext("idx/segments/1601/01/01/0000/meta.json")
//            .expectNext("idx/segments/2019/11/01/1700/meta.json")
//            .expectNext("idx/segments/2020/01/16/2300/meta.json")
//            .expectNext("idx/segments/2020/03/02/2300/meta.json")
//            .expectNext("idx/segments/2020/03/03/0000/meta.json")
//            .expectNext("idx/segments/2020/03/03/1800/meta.json")
//            .expectNext("idx/segments/2020/03/03/2000/meta.json")
//            .expectNext("idx/segments/2020/03/03/2200/meta.json")
//            .expectNext("idx/segments/2020/03/05/1700/meta.json")
//            .expectNext("idx/segments/2020/03/12/2200/meta.json")
//            .expectNext("idx/segments/2020/03/19/2200/meta.json")
//            .expectNext("idx/segments/2020/03/23/2200/meta.json")
//            .expectComplete()
//            .verify();
//    }
//
//    def "get shards"() {
//        setup:
//        def client = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient('$blobchangefeed')
//        // Years
//        def segments = client.listBlobsByHierarchy("idx/segments/")
//            .concatMap({ year ->
//                return client.listBlobs(new ListBlobsOptions().setPrefix(year.getName()))
//            })
//            .map({ segment -> segment.getName() })
//
//        when:
//        def sv = StepVerifier.create(
//            segments.map({ segment ->
//                return client.getBlobAsyncClient(segment)
//            }).flatMapSequential({ segmentClient ->
//                segmentClient.download().reduce(new ByteArrayOutputStream(), { outputStream, buffer ->
//                    outputStream.write(FluxUtil.byteBufferToArray(buffer));
//                    return outputStream;
//                })
//            }).map({ os ->
//                os.toString()
//            }).flatMapIterable({ json ->
//                ObjectMapper objectMapper = new ObjectMapper()
//                JsonNode jsonNode = objectMapper.readTree(json)
//                Iterable<String> shards = new ArrayList<>()
//                for (JsonNode shard : jsonNode.withArray("chunkFilePaths")) {
//                    shards.add(shard.asText().substring('$blobchangefeed/'.length()))
//                }
//                return shards
//            }).map({ shard ->
//                System.out.println(shard)
//                return shard
//            })
//        )
//        then:
//        sv.expectNext("log/00/1601/01/01/0000/")
//            .expectNext("log/00/2019/11/01/1700/")
//            .expectNext("log/00/2020/01/16/2300/")
//            .expectNext("log/00/2020/03/02/2300/")
//            .expectNext("log/00/2020/03/03/0000/")
//            .expectNext("log/00/2020/03/03/1800/")
//            .expectNext("log/00/2020/03/03/2000/")
//            .expectNext("log/00/2020/03/03/2200/")
//            .expectNext("log/00/2020/03/05/1700/")
//            .expectNext("log/00/2020/03/12/2200/")
//            .expectNext("log/00/2020/03/19/2200/")
//            .expectNext("log/00/2020/03/23/2200/")
//            .expectComplete()
//            .verify();
//    }
//
//    def "get chunks"() {
//        setup:
//        def client = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient('$blobchangefeed')
//        // Years
//        def shards = client.listBlobsByHierarchy("idx/segments/")
//            .flatMapSequential({ year ->
//                return client.listBlobs(new ListBlobsOptions().setPrefix(year.getName()))
//            })
//            .map({ segment -> segment.getName() })
//            .map({ segment ->
//            return client.getBlobAsyncClient(segment)
//        }).flatMapSequential({ segmentClient ->
//            segmentClient.download().reduce(new ByteArrayOutputStream(), { outputStream, buffer ->
//                outputStream.write(FluxUtil.byteBufferToArray(buffer));
//                return outputStream;
//            })
//        }).map({ os ->
//            os.toString()
//        }).flatMapIterable({ json ->
//            ObjectMapper objectMapper = new ObjectMapper()
//            JsonNode jsonNode = objectMapper.readTree(json)
//            Iterable<String> shards = new ArrayList<>()
//            for (JsonNode shard : jsonNode.withArray("chunkFilePaths")) {
//                shards.add(shard.asText().substring('$blobchangefeed/'.length()))
//            }
//            return shards
//        })
//
//        when:
//        def sv = StepVerifier.create(
//            shards.flatMapSequential({ shard ->
//            return client.listBlobs(new ListBlobsOptions().setPrefix(shard))
//        }).map({ chunk -> chunk.getName() })
//        )
//        then:
//        sv.expectNext("log/00/2019/11/01/1700/00000.avro")
//            .expectNext("log/00/2020/01/16/2300/00000.avro")
//            .expectNext("log/00/2020/03/02/2300/00000.avro")
//            .expectNext("log/00/2020/03/03/0000/00000.avro")
//            .expectNext("log/00/2020/03/03/1800/00000.avro")
//            .expectNext("log/00/2020/03/03/2000/00000.avro")
//            .expectNext("log/00/2020/03/03/2200/00000.avro")
//            .expectNext("log/00/2020/03/05/1700/00000.avro")
//            .expectNext("log/00/2020/03/12/2200/00000.avro")
//            .expectNext("log/00/2020/03/19/2200/00000.avro")
//            .expectNext("log/00/2020/03/23/2200/00000.avro")
//            .expectComplete()
//            .verify();
//    }
//
//    def "parse chunks"() {
//        setup:
//        def client = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient('$blobchangefeed')
//        // Years
//        def shards = client.listBlobsByHierarchy("idx/segments/")
//            .flatMapSequential({ year ->
//                return client.listBlobs(new ListBlobsOptions().setPrefix(year.getName()))
//            })
//            .map({ segment -> segment.getName() })
//            .map({ segment ->
//                return client.getBlobAsyncClient(segment)
//            }).flatMapSequential({ segmentClient ->
//            segmentClient.download().reduce(new ByteArrayOutputStream(), { outputStream, buffer ->
//                outputStream.write(FluxUtil.byteBufferToArray(buffer));
//                return outputStream;
//            })
//        }).map({ os ->
//            os.toString()
//        }).flatMapIterable({ json ->
//            ObjectMapper objectMapper = new ObjectMapper()
//            JsonNode jsonNode = objectMapper.readTree(json)
//            Iterable<String> shards = new ArrayList<>()
//            for (JsonNode shard : jsonNode.withArray("chunkFilePaths")) {
//                shards.add(shard.asText().substring('$blobchangefeed/'.length()))
//            }
//            return shards
//        }).flatMapSequential({ shard ->
//            return client.listBlobs(new ListBlobsOptions().setPrefix(shard))
//        }).map({ chunk -> chunk.getName() })
//
//        when:
//        def sv = StepVerifier.create(
//            shards
//                .map({ shard ->
//                    return client.getBlobAsyncClient(shard)
//                }).flatMapSequential({ shardClient ->
//                shardClient.download().reduce(new ByteArrayOutputStream(), { outputStream, buffer ->
//                    outputStream.write(FluxUtil.byteBufferToArray(buffer));
//                    return outputStream;
//                })
//            }).map({ os ->
//                return new ByteArrayInputStream(os.toByteArray())
//            }).flatMapIterable({ avro ->
//                DataFileStream<GenericRecord> parsedStream = new DataFileStream<>(avro, new GenericDatumReader<>())
//                Iterable<BlobChangefeedEvent> events = new ArrayList<>()
//                while (parsedStream.hasNext()) {
//                    GenericRecord r = parsedStream.next()
//                    GenericRecord d = r.get("data")
//                    events.add(new BlobChangefeedEvent(r.get("topic").toString(),
//                        r.get("subject").toString(),
//                        BlobChangefeedEventType.fromString(r.get("eventType").toString()),
//                        OffsetDateTime.parse(r.get("eventTime").toString()),
//                        r.get("id").toString(),
//                        new BlobChangefeedEventData(d.get("api").toString(),
//                            d.get("clientRequestId").toString(),
//                            null,
//                            d.get("etag").toString(),
//                            d.get("contentType").toString(),
//                            0,
//                            BlobType.fromString(d.get("blobType").toString()),
//                            null, null, null,
//                            d.get("url").toString(),
//                            null,
//                            d.get("sequencer").toString()),
//                        null,
//                        r.get("metadataVersion").toString()))
//                }
//                return events
//            })
//        )
//        then:
//        sv.expectNextCount(1133)
//        .verifyComplete()
//    }
//
//    def "final cf"() {
//        setup:
//        def client = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient('$blobchangefeed')
//        // Years
//        def events = client.listBlobsByHierarchy("idx/segments/")
//            .flatMapSequential({ year ->
//                System.out.println("YEAR : " + year.getName())
//                return client.listBlobs(new ListBlobsOptions().setPrefix(year.getName()))
//            })
//            .flatMapSequential({ segment ->
//            System.out.println("SEGMENT : " + segment.getName())
//            client.getBlobAsyncClient(segment.getName()).download().reduce(new ByteArrayOutputStream(), { outputStream, buffer ->
//                outputStream.write(FluxUtil.byteBufferToArray(buffer));
//                return outputStream;
//            })
//            }).map({ os ->
//            os.toString()
//        }).flatMapIterable({ json ->
//            ObjectMapper objectMapper = new ObjectMapper()
//            JsonNode jsonNode = objectMapper.readTree(json)
//            Iterable<String> shards = new ArrayList<>()
//            for (JsonNode shard : jsonNode.withArray("chunkFilePaths")) {
//                shards.add(shard.asText().substring('$blobchangefeed/'.length()))
//            }
//            return shards
//        }).flatMapSequential({ shard ->
//            System.out.println("SHARD : " + shard)
//            return client.listBlobs(new ListBlobsOptions().setPrefix(shard))
//        }).flatMapSequential({ chunk ->
//            System.out.println("CHUNK : " + chunk.getName())
//            client.getBlobAsyncClient(chunk.getName()).download().reduce(new ByteArrayOutputStream(), { outputStream, buffer ->
//                outputStream.write(FluxUtil.byteBufferToArray(buffer));
//                return outputStream;
//            })
//        }).map({ os ->
//            return new ByteArrayInputStream(os.toByteArray())
//        }).flatMapIterable({ avro ->
//            DataFileStream<GenericRecord> parsedStream = new DataFileStream<>(avro, new GenericDatumReader<>())
//            Iterable<BlobChangefeedEvent> events = new ArrayList<>()
//            while (parsedStream.hasNext()) {
//                GenericRecord r = parsedStream.next()
//                GenericRecord d = r.get("data")
//                events.add(new BlobChangefeedEvent(r.get("topic").toString(),
//                    r.get("subject").toString(),
//                    BlobChangefeedEventType.fromString(r.get("eventType").toString()),
//                    OffsetDateTime.parse(r.get("eventTime").toString()),
//                    r.get("id").toString(),
//                    new BlobChangefeedEventData(d.get("api").toString(),
//                        d.get("clientRequestId").toString(),
//                        null,
//                        d.get("etag").toString(),
//                        d.get("contentType").toString(),
//                        0,
//                        BlobType.fromString(d.get("blobType").toString()),
//                        null, null, null,
//                        d.get("url").toString(),
//                        null,
//                        d.get("sequencer").toString()),
//                    null,
//                    r.get("metadataVersion").toString()))
//            }
//            return events
//        })
//
//        when:
//        def sv = StepVerifier.create(
//            events
//        )
//        then:
//        sv.expectNextCount(1201).verifyComplete()
//    }
//
//    def "real cf"() {
//        when:
//        def sv = StepVerifier.create(
//            new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
//            .buildAsyncClient().getEvents()
//        )
//        then:
//        sv.expectNextCount(1202)
//            .verifyComplete()
//    }
//
//    def "real cf start end"() {
//        when:
//        def sv = StepVerifier.create(
//            new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
//                .buildAsyncClient().getEvents(null, TimeUtils.roundDownToNearestYear(OffsetDateTime.now()))
//        )
//        then:
//        sv.expectNextCount(1)
//            .verifyComplete()
//    }
//
//    def "real cf paged"() {
//        setup:
//        BlobChangefeedPagedFlux pagedFlux = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
//            .buildAsyncClient().getEvents()
//
//        when:
//        def sv = StepVerifier.create(
//            pagedFlux
//        )
//        then:
//        sv.expectNextCount(1286).verifyComplete()
//    }
//
//    def "real cf paged 50"() {
//        setup:
//        BlobChangefeedPagedFlux pagedFlux = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
//            .buildAsyncClient().getEvents()
//
//        when:
//        def sv = StepVerifier.create(
//            pagedFlux.byPage(50)
//        )
//        then:
//        sv.expectNextCount(26).verifyComplete()
//    }
//
//    def "real cf actually paged"() {
//        setup:
//
//        BlobChangefeedAsyncClient client = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
//            .buildAsyncClient()
//
//        BlobChangefeedPagedFlux pagedFlux = client.getEvents("{\"endTime\":\"+999999999-12-31T23:59:59.999999999-18:00\",\"segmentTime\":\"2020-03-23T22:00Z\",\"shardPath\":\"log/00/2020/03/23/2200/\",\"chunkPath\":\"log/00/2020/03/23/2200/00000.avro\",\"eventIndex\":35,\"eventToBeProcessed\":null}")
//        def sv = StepVerifier.create(
//            pagedFlux.byPage(50)
//        )
//
//        sv.expectNextCount(23).verifyComplete()
//
//    }
//
//    def "real cf actually paged sync"() {
//        setup:
//        BlobChangefeedClient client = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
//            .buildClient()
//
//        BlobChangefeedPagedIterable pagedIterable = client.getEvents()
//            def it = pagedIterable.iterator()
//
//        int num = 0;
//
//        while (it.hasNext()) {
//            it.next()
//            num++
//        }
//
//        assert num == 1286
//    }
//
//    def "real cf paged sync"() {
//        setup:
//        BlobChangefeedClient client = new BlobChangefeedClientBuilder(primaryBlobServiceAsyncClient)
//            .buildClient()
//
//        def pagedIterable = client.getEvents().iterableByPage(50)
//
//        def num = 0
//        for (def item : pagedIterable) {
//            num++
//        }
//
//        assert num == 26
//    }
}
