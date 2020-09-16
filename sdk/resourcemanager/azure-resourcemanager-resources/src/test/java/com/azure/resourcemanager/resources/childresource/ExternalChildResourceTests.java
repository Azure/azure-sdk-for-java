// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.childresource;

import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ExternalChildResourceTests {
    @Test
    public void noCommitIfNoChange() throws InterruptedException {
        ChickenImpl chicken = new ChickenImpl(); // Parent resource
        PulletsImpl pullets = chicken.pullets(); // Child resource collection
        final CountDownLatch monitor = new CountDownLatch(1);
        // Note that commitAsync() won't be exposed to the end-user as it's a part of child resource impl
        // pullets.commitAsync will be called from (Applicable)chicken.applyAsync() or (Creatable)chicken.createAsyncStreaming().
        //
        // Observable<Chicken> Chicken::ApplyAsync() {
        //      [1] update chicken
        //      [2] update pullets by calling pullets.commitAsync()
        // }
        //
        // In the unit test cases we call it directly as we testing external child resource here.
        //
        pullets.commitAsync()
            .subscribe(
                pullet -> Assertions.assertTrue(false, "nothing to commit onNext should not be invoked"),

                throwable -> {
                    monitor.countDown();
                    Assertions.assertTrue(false, "nothing to commit onError should not be invoked");
                },
                () -> monitor.countDown()
            );
        monitor.await();
    }

    @Test
    public void shouldCommitCreateUpdateAndDelete() throws InterruptedException {
        ChickenImpl chicken = new ChickenImpl(); // Parent resource
        chicken
                .defineNewPullet("alice")
                .withAge(1)
                .attach()
                .updatePullet("Clover")
                .withAge(2)
                .parent()
                .withoutPullet("Pinky");

        final List<PulletImpl> changedPuppets = new ArrayList<>();
        final CountDownLatch monitor = new CountDownLatch(1);

        PulletsImpl pullets = chicken.pullets();
        pullets.commitAsync().subscribe(
            pullet -> changedPuppets.add(pullet),
            throwable -> {
                monitor.countDown();
                Assertions.assertTrue(false, "onError should not be invoked");
            },
            () -> monitor.countDown()
        );

        monitor.await();
        Assertions.assertTrue(changedPuppets.size() == 3);
        for (PulletImpl pullet : changedPuppets) {
            Assertions.assertTrue(pullet.pendingOperation() == ExternalChildResourceImpl.PendingOperation.None);
        }
    }

    @Test
    public void shouldEmitErrorAfterAllSuccessfulCommit() throws InterruptedException {
        ChickenImpl chicken = new ChickenImpl(); // Parent resource
        chicken
                .defineNewPullet("alice")
                .withAge(1)
                .withFailFlag(PulletImpl.FailFlag.OnCreate)
                .attach()
                .updatePullet("Clover")
                .withAge(2)
                .parent()
                .updatePullet("Goldilocks")
                .withAge(2)
                .withFailFlag(PulletImpl.FailFlag.OnUpdate)
                .parent()
                .withoutPullet("Pinky");

        final List<PulletImpl> changedPuppets = new ArrayList<>();
        final List<Throwable> throwables = new ArrayList<>();
        final CountDownLatch monitor = new CountDownLatch(1);
        PulletsImpl pullets = chicken.pullets();
        pullets.commitAsync()
            .subscribe(
                pullet -> changedPuppets.add(pullet),
                throwable -> {
                    try {
                        Throwable[] exception = throwable.getSuppressed();
                        Assertions.assertNotNull(exception);
                        for (Throwable innerThrowable : exception) {
                            throwables.add(innerThrowable);
                        }
                    } finally {
                        monitor.countDown();
                    }
                },
                () -> {
                    monitor.countDown();
                    Assertions.assertTrue(false, "onCompleted should not be invoked");
                }
            );

        monitor.await();
        Assertions.assertTrue(throwables.size() == 2);
        Assertions.assertTrue(changedPuppets.size() == 2);
    }

    @Test
    public void canStreamAccumulatedResult() throws InterruptedException {
        ChickenImpl chicken = new ChickenImpl();
        chicken
                .defineNewPullet("alice")
                .withAge(1)
                .attach()
                .updatePullet("Clover")
                .withAge(2)
                .attach()
                .withoutPullet("Pinky");

        PulletsImpl pullets = chicken.pullets();
        final CountDownLatch monitor = new CountDownLatch(1);
        pullets.commitAndGetAllAsync()
            .subscribe(lets -> Assertions.assertTrue(lets.size() == 3),
                throwable -> {
                    monitor.countDown();
                    Assertions.assertTrue(false, "onError should not be invoked");
                },
                () -> monitor.countDown()
            );


        monitor.await();
    }

    @Test
    public void canCrossReferenceChildren() throws Exception {
        SchoolsImpl schools = new SchoolsImpl();

        Flux<Indexable> items = schools.define("redmondSchool")
                .withAddress("sc-address")
                .defineTeacher("maria")
                .withSubject("Maths")
                .attach()
                .defineStudent("bob")
                .withAge(10)
                .withTeacher("maria")   // Refer another creatable external child resource with key 'maria' in the parent
                .attach()
                .createAsync();

        final SchoolsImpl.SchoolImpl[] foundSchool = new SchoolsImpl.SchoolImpl[1];
        final SchoolsImpl.TeacherImpl[] foundTeacher = new SchoolsImpl.TeacherImpl[1];
        final SchoolsImpl.StudentImpl[] foundStudent = new SchoolsImpl.StudentImpl[1];

        items.doOnNext(indexable -> {
            if (indexable instanceof SchoolsImpl.SchoolImpl) {
                foundSchool[0] = (SchoolsImpl.SchoolImpl) indexable;
            }
            if (indexable instanceof SchoolsImpl.TeacherImpl) {
                foundTeacher[0] = (SchoolsImpl.TeacherImpl) indexable;
            }
            if (indexable instanceof SchoolsImpl.StudentImpl) {
                foundStudent[0] = (SchoolsImpl.StudentImpl) indexable;
            }
        }).blockLast();

        Assertions.assertNotNull(foundSchool[0]);
        Assertions.assertNotNull(foundTeacher[0]);
        Assertions.assertNotNull(foundStudent[0]);

        Assertions.assertTrue(foundSchool[0].isInvoked());
        Assertions.assertTrue(foundTeacher[0].isInvoked());
        Assertions.assertTrue(foundStudent[0].isInvoked());
    }

    @Test
    public void canCreateChildrenIndependently() throws Exception {
        SchoolsImpl schools = new SchoolsImpl();

        Creatable<SchoolsImpl.TeacherImpl> creatableTeacher = schools.independentTeachers()
                .define("john")
                .withSubject("physics");

        SchoolsImpl.StudentImpl creatableStudent = schools.independentStudents()
                .define("nit")
                .withAge(15)
                .withTeacher(creatableTeacher);

        final SchoolsImpl.TeacherImpl[] foundTeacher = new SchoolsImpl.TeacherImpl[1];
        final SchoolsImpl.StudentImpl[] foundStudent = new SchoolsImpl.StudentImpl[1];

        creatableStudent.taskGroup().invokeAsync(creatableStudent.taskGroup().newInvocationContext())
            .doOnNext(indexable -> {
                if (indexable instanceof SchoolsImpl.TeacherImpl) {
                    foundTeacher[0] = (SchoolsImpl.TeacherImpl) indexable;
                }
                if (indexable instanceof SchoolsImpl.StudentImpl) {
                    foundStudent[0] = (SchoolsImpl.StudentImpl) indexable;
                }
            }).blockLast();

        Assertions.assertNotNull(foundTeacher[0]);
        Assertions.assertNotNull(foundStudent[0]);

        Assertions.assertTrue(foundTeacher[0].isInvoked());
        Assertions.assertTrue(foundStudent[0].isInvoked());
    }
}
