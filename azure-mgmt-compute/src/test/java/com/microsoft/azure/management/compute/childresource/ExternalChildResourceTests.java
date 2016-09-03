package com.microsoft.azure.management.compute.childresource;

import com.microsoft.azure.management.compute.implementation.ExternalChildResourceImpl;
import org.junit.Assert;
import org.junit.Test;
import rx.Observer;
import rx.Subscriber;
import rx.exceptions.CompositeException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ExternalChildResourceTests {
    @Test
    public void noCommitIfNoChange() throws InterruptedException {
        ChickenImpl chicken = new ChickenImpl();
        PulletsImpl pullets = chicken.pullets();
        final CountDownLatch monitor = new CountDownLatch(1);
        pullets.commitAsync()
                .subscribe(new Subscriber<PulletImpl>() {
                    @Override
                    public void onCompleted() {
                        monitor.countDown();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        monitor.countDown();
                        Assert.assertTrue("nothing to commit onError should not be invoked", false);
                    }

                    @Override
                    public void onNext(PulletImpl pullet) {
                        Assert.assertTrue("nothing to commit onNext should not be invoked", false);
                    }
                });
        monitor.await();
    }

    @Test
    public void shouldCommitCreateUpdateAndDelete() throws InterruptedException {
        ChickenImpl chicken = new ChickenImpl();
        chicken
            .defineNewPullet("alice")
                .withAge(1)
                .attach()
            .updatePullet("Clover")
                .withAge(2)
                .attach()
            .withoutPullet("Pinky");

        final List<PulletImpl> changedPuppets = new ArrayList<>();
        final CountDownLatch monitor = new CountDownLatch(1);
        PulletsImpl pullets = chicken.pullets();
        pullets.commitAsync()
                .subscribe(new Observer<PulletImpl>() {
                    @Override
                    public void onCompleted() {
                        monitor.countDown();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        monitor.countDown();
                        Assert.assertTrue("onError should not be invoked", false);
                    }

                    @Override
                    public void onNext(PulletImpl pullet) {
                        changedPuppets.add(pullet);
                    }
                });
        monitor.await();
        Assert.assertTrue(changedPuppets.size() == 3);
        for (PulletImpl pullet : changedPuppets) {
            Assert.assertTrue(pullet.state() == ExternalChildResourceImpl.State.None);
        }
    }

    @Test
    public void shouldEmitErrorAfterAllSuccessfulCommit() throws InterruptedException {
        ChickenImpl chicken = new ChickenImpl();
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
                .subscribe(new Observer<PulletImpl>() {
                    @Override
                    public void onCompleted() {
                        monitor.countDown();
                        Assert.assertTrue("onCompleted should not be invoked", false);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        try {
                            CompositeException exception = (CompositeException) throwable;
                            Assert.assertNotNull(exception);
                            for (Throwable innerThrowable : exception.getExceptions()) {
                                throwables.add(innerThrowable);
                            }
                        } finally {
                            monitor.countDown();
                        }
                    }

                    @Override
                    public void onNext(PulletImpl pullet) {
                        changedPuppets.add(pullet);
                    }
                });

        monitor.await();
        Assert.assertTrue(throwables.size() == 2);
        Assert.assertTrue(changedPuppets.size() == 2);
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
                .subscribe(new Subscriber<List<PulletImpl>>() {
                    @Override
                    public void onCompleted() {
                        monitor.countDown();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        monitor.countDown();
                        Assert.assertTrue("onError should not be invoked", false);
                    }

                    @Override
                    public void onNext(List<PulletImpl> pullets) {
                        Assert.assertTrue(pullets.size() == 3);
                    }
                });
        monitor.await();
    }
}
