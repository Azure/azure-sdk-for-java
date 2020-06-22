/*
 * Copyright 2015-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package web.service.springdata.gremlin.web.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import web.service.springdata.gremlin.domain.MicroService;
import web.service.springdata.gremlin.domain.ServicesDataFlow;
import web.service.springdata.gremlin.repository.MicroServiceRepository;
import web.service.springdata.gremlin.repository.ServicesDataFlowRepository;
import web.service.springdata.gremlin.web.domain.Greeting;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class WebController {

    @Autowired
    private MicroServiceRepository microServiceRepo;

    @Autowired
    private ServicesDataFlowRepository dataFlowRepo;

    private final AtomicLong counter = new AtomicLong();

    @RequestMapping(value = "/greeting", method = RequestMethod.GET)
    public Greeting greeting() {
        return new Greeting(String.valueOf(this.counter.incrementAndGet()), "Greetings to User.");
    }

    @RequestMapping(value = "/services/{id}", method = RequestMethod.GET)
    public MicroService getService(@PathVariable String id) {

        final Optional<MicroService> foundService = this.microServiceRepo.findById(id);

        return foundService.orElse(null);
    }

    @RequestMapping(value = "/services/{id}", method = RequestMethod.PUT)
    public MicroService putService(@PathVariable String id, @RequestBody MicroService service) {

        if (!service.getId().equals(id)) {
            service.setId(id);
        }

        this.microServiceRepo.save(service);

        return service;
    }

    @RequestMapping(value = "/services/{id}", method = RequestMethod.DELETE)
    public void deleteService(@PathVariable String id) {
        this.microServiceRepo.deleteById(id);
    }

    @RequestMapping(value = "/services/", method = RequestMethod.DELETE)
    public void deleteService(@RequestBody MicroService service) {
        this.microServiceRepo.delete(service);
    }

    @RequestMapping(value = "/services/all", method = RequestMethod.DELETE)
    public void deleteServicesAll() {
        this.microServiceRepo.deleteAll();
    }

    @RequestMapping(value = "/services/", method = RequestMethod.PUT)
    public MicroService putService(@RequestBody MicroService service) {

        this.microServiceRepo.save(service);

        return service;
    }

    @RequestMapping(value = "/services/", method = RequestMethod.GET)
    public List<MicroService> getServiceList() {
        return (List<MicroService>) this.microServiceRepo.findAll(MicroService.class);
    }

    @RequestMapping(value = "/services/create/{id}", method = RequestMethod.PUT)
    public MicroService createService(@PathVariable String id, @RequestBody MicroService service) {
        return this.putService(id, service);
    }

    @RequestMapping(value = "/services/create/", method = RequestMethod.PUT)
    public MicroService createService(@RequestBody MicroService service) {
        return this.putService(service);
    }

    @RequestMapping(value = "/dataflow/{id}", method = RequestMethod.GET)
    public ServicesDataFlow getServicesDataFlow(@PathVariable String id) {

        final Optional<ServicesDataFlow> foundDataFlow = this.dataFlowRepo.findById(id);

        return foundDataFlow.orElse(null);
    }

    @RequestMapping(value = "/dataflow/{id}", method = RequestMethod.PUT)
    public ServicesDataFlow putServicesDataFlow(@PathVariable String id, @RequestBody ServicesDataFlow dataFlow) {

        if (!dataFlow.getId().equals(id)) {
            dataFlow.setId(id);
        }

        this.dataFlowRepo.save(dataFlow);

        return dataFlow;
    }

    @RequestMapping(value = "/dataflow/", method = RequestMethod.PUT)
    public ServicesDataFlow putServicesDataFlow(@RequestBody ServicesDataFlow dataFlow) {

        this.dataFlowRepo.save(dataFlow);

        return dataFlow;
    }

    @RequestMapping(value = "/dataflow/", method = RequestMethod.GET)
    public List<ServicesDataFlow> getServicesDataFlowList() {
        return (List<ServicesDataFlow>) this.dataFlowRepo.findAll(ServicesDataFlow.class);
    }
}
