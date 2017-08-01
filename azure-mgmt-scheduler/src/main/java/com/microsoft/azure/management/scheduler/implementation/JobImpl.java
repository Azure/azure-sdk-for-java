/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.scheduler.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.models.IndependentChild;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.IndependentChildImpl;
import com.microsoft.azure.management.scheduler.DayOfWeek;
import com.microsoft.azure.management.scheduler.Job;
import com.microsoft.azure.management.scheduler.JobAction;
import com.microsoft.azure.management.scheduler.JobCollection;
import com.microsoft.azure.management.scheduler.JobRecurrence;
import com.microsoft.azure.management.scheduler.JobRecurrenceSchedule;
import com.microsoft.azure.management.scheduler.JobRecurrenceScheduleMonthlyOccurrence;
import com.microsoft.azure.management.scheduler.JobScheduleDay;
import com.microsoft.azure.management.scheduler.JobScheduleMonthlyWeekDay;
import com.microsoft.azure.management.scheduler.JobState;
import com.microsoft.azure.management.scheduler.JobStatus;
import com.microsoft.azure.management.scheduler.RecurrenceFrequency;
import org.joda.time.DateTime;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;

/**
 * Implementation for Azure Scheduler Job.
 */
@LangDefinition
public class JobImpl
        extends IndependentChildImpl<
                                Job,
                                JobCollection,
                                JobDefinitionInner,
                                JobImpl,
                                ScheduleServiceManager>
        implements
            Job,
            Job.Definition,
            Job.Update,
            Job.UpdateStages.UpdateOptionals,
            IndependentChild.DefinitionStages.WithParentResource<Job, JobCollection> {

    /**
     * Creates a new instance of IndependentChildResourceImpl.
     *
     * @param name        the name of the resource
     * @param innerObject the inner object
     * @param manager
     */
    protected JobImpl(String resourceGroupName, String jobCollectionName, String name, JobDefinitionInner innerObject, ScheduleServiceManager manager) {
        super(name, innerObject, manager);
        this.withExistingParentResource(resourceGroupName, jobCollectionName);
    }

    @Override
    public String name() {
        // The service does not return the simple name of the job but the "jobCollectionName/jobName"
        String[] splitName = super.name().split("/");
        return splitName[splitName.length - 1];
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String jobCollectionName() {
        return this.parentName;
    }

    @Override
    public DateTime startTime() {
        return inner().properties().startTime();
    }

    @Override
    public JobAction action() {
        return inner().properties().action();
    }

    @Override
    public JobRecurrence recurrence() {
        return inner().properties().recurrence();
    }

    @Override
    public JobState state() {
        return inner().properties().state();
    }

    @Override
    public JobStatus status() {
        return inner().properties().status();
    }

    @Override
    protected Observable<JobDefinitionInner> getInnerAsync() {
        return this.manager().inner().jobs().getAsync(this.resourceGroupName(), this.jobCollectionName(), this.name());
    }

    @Override
    protected Observable<Job> createChildResourceAsync() {
        final JobImpl self = this;
        return this.manager().inner().jobs().createOrUpdateAsync(this.resourceGroupName(), this.jobCollectionName(), this.name(), this.inner().properties())
            .map(new Func1<JobDefinitionInner, Job>() {
                @Override
                public Job call(JobDefinitionInner jobDefinitionInner) {
                    setInner(jobDefinitionInner);
                    return self;
                }
            });
    }

    @Override
    public JobImpl startingNow() {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
        }
        this.inner().properties().withStartTime(null);

        return this;
    }

    @Override
    public JobImpl startingAt(DateTime startTime) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
        }
        this.inner().properties().withStartTime(startTime);

        return this;
    }

    @Override
    public JobImpl withStartTime(DateTime startTime) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
        }
        this.inner().properties().withStartTime(startTime);

        return this;
    }

    @Override
    public JobImpl withRecurrence(JobRecurrence jobRecurrence) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
        }
        this.inner().properties().withRecurrence(jobRecurrence);

        return this;
    }

    @Override
    public JobImpl runningEvery(int interval) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
        }
        this.inner().properties().withRecurrence(new JobRecurrence().withInterval(interval));

        return this;
    }

    @Override
    public JobImpl runningEveryMinute() {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
        }
        this.inner().properties()
            .withRecurrence(new JobRecurrence()
                .withInterval(1)
                .withFrequency(RecurrenceFrequency.MINUTE));

        return this;
    }

    @Override
    public JobImpl runningHourly() {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
        }
        this.inner().properties()
            .withRecurrence(new JobRecurrence()
                .withInterval(1)
                .withFrequency(RecurrenceFrequency.HOUR));

        return this;
    }

    @Override
    public JobImpl runningDaily() {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
        }
        this.inner().properties()
            .withRecurrence(new JobRecurrence()
                .withInterval(1)
                .withFrequency(RecurrenceFrequency.DAY));

        return this;
    }

    @Override
    public JobImpl runningWeekly() {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
        }
        this.inner().properties()
            .withRecurrence(new JobRecurrence()
                .withInterval(1)
                .withFrequency(RecurrenceFrequency.WEEK));

        return this;
    }

    @Override
    public JobImpl runningMonthly() {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
        }
        this.inner().properties()
            .withRecurrence(new JobRecurrence()
                .withInterval(1)
                .withFrequency(RecurrenceFrequency.MONTH));

        return this;
    }

    @Override
    public JobImpl withAction(JobAction jobAction) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
        }
        this.inner().properties().withAction(jobAction);

        return this;
    }

    @Override
    public JobImpl withState(JobState state) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
        }
        this.inner().properties().withState(state);

        return this;
    }

    @Override
    public JobImpl minutes() {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
            this.inner().properties()
                .withRecurrence(new JobRecurrence()
                    .withInterval(1));
        }
        this.inner().properties()
            .recurrence()
                .withFrequency(RecurrenceFrequency.MINUTE);

        return this;
    }

    @Override
    public JobImpl hours() {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
            this.inner().properties()
                .withRecurrence(new JobRecurrence()
                    .withInterval(1));
        }
        this.inner().properties()
            .recurrence()
            .withFrequency(RecurrenceFrequency.HOUR);

        return this;
    }

    @Override
    public JobImpl days() {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
            this.inner().properties()
                .withRecurrence(new JobRecurrence()
                    .withInterval(1));
        }
        this.inner().properties()
            .recurrence()
            .withFrequency(RecurrenceFrequency.DAY);

        return this;
    }

    @Override
    public JobImpl weeks() {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
            this.inner().properties()
                .withRecurrence(new JobRecurrence()
                    .withInterval(1));
        }
        this.inner().properties()
            .recurrence()
            .withFrequency(RecurrenceFrequency.WEEK);

        return this;
    }

    @Override
    public JobImpl months() {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
            this.inner().properties()
                .withRecurrence(new JobRecurrence()
                    .withInterval(1));
        }
        this.inner().properties()
            .recurrence()
            .withFrequency(RecurrenceFrequency.MONTH);

        return this;
    }

    private void resetJobScheduleDaysList() {
        // Only one days list can be specified, other scheduler lists need to be set to null
        if (this.inner().properties() != null &&
            inner().properties().recurrence() != null &&
            inner().properties().recurrence().schedule() != null) {
            this.inner().properties()
                .recurrence()
                .schedule()
                .withWeekDays(null);
            this.inner().properties()
                .recurrence()
                .schedule()
                .withMonthDays(null);
            this.inner().properties()
                .recurrence()
                .schedule()
                .withMonthlyOccurrences(null);
        }
    }

    @Override
    public JobImpl onTheseDays(DayOfWeek... days) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
            this.inner().properties()
                .withRecurrence(new JobRecurrence());
        }
        if (this.inner().properties().recurrence().schedule() == null) {
            this.inner().properties()
                .recurrence()
                .withSchedule(new JobRecurrenceSchedule());
        }
        resetJobScheduleDaysList();

        this.inner().properties()
            .recurrence()
            .schedule().withWeekDays(new ArrayList<DayOfWeek>());
        for (DayOfWeek dayOfWeek : days) {
            this.inner().properties().recurrence().schedule().weekDays().add(dayOfWeek);
        }

        return this;
    }

    @Override
    public JobImpl onTheseDaysOfMonth(int... days) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
            this.inner().properties()
                .withRecurrence(new JobRecurrence());
        }
        if (this.inner().properties().recurrence().schedule() == null) {
            this.inner().properties()
                .recurrence()
                .withSchedule(new JobRecurrenceSchedule());
        }
        resetJobScheduleDaysList();

        this.inner().properties()
            .recurrence()
            .schedule().withMonthDays(new ArrayList<Integer>());
        for (int day : days) {
            this.inner().properties().recurrence().schedule().monthDays().add(day);
        }

        return this;
    }

    @Override
    public JobImpl recurringEvery(JobScheduleMonthlyWeekDay... weekDays) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
            this.inner().properties()
                .withRecurrence(new JobRecurrence());
        }
        if (this.inner().properties().recurrence().schedule() == null) {
            this.inner().properties()
                .recurrence()
                .withSchedule(new JobRecurrenceSchedule());
        }
        resetJobScheduleDaysList();

        this.inner().properties()
            .recurrence()
            .schedule().withMonthlyOccurrences(new ArrayList<JobRecurrenceScheduleMonthlyOccurrence>());
        for (JobScheduleMonthlyWeekDay day : weekDays) {
            this.inner().properties().recurrence().schedule().monthlyOccurrences().add(day.monthlyOccurrence());
        }

        return this;
    }

    @Override
    public JobImpl recurringEvery(JobScheduleDay... weekDays) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
            this.inner().properties()
                .withRecurrence(new JobRecurrence());
        }
        if (this.inner().properties().recurrence().schedule() == null) {
            this.inner().properties()
                .recurrence()
                .withSchedule(new JobRecurrenceSchedule());
        }
        resetJobScheduleDaysList();

        this.inner().properties()
            .recurrence()
            .schedule().withMonthlyOccurrences(new ArrayList<JobRecurrenceScheduleMonthlyOccurrence>());
        for (JobScheduleDay day : weekDays) {
            this.inner().properties().recurrence().schedule()
                .monthlyOccurrences().add(new JobRecurrenceScheduleMonthlyOccurrence().withDay(day));
        }

        return this;
    }

    @Override
    public JobImpl atTheseMinutes(int... minutes) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
            this.inner().properties()
                .withRecurrence(new JobRecurrence());
        }
        if (this.inner().properties().recurrence().schedule() == null) {
            this.inner().properties()
                .recurrence()
                .withSchedule(new JobRecurrenceSchedule());
        }
        if (this.inner().properties().recurrence().schedule().minutes() == null) {
            this.inner().properties()
                .recurrence()
                .schedule().withMinutes(new ArrayList<Integer>());
        }
        for (int minute : minutes) {
            this.inner().properties().recurrence().schedule().minutes().add(minute);
        }

        return this;
    }

    @Override
    public JobImpl atTheseHours(int... hours) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
            this.inner().properties()
                .withRecurrence(new JobRecurrence());
        }
        if (this.inner().properties().recurrence().schedule() == null) {
            this.inner().properties()
                .recurrence()
                .withSchedule(new JobRecurrenceSchedule());
        }
        if (this.inner().properties().recurrence().schedule().hours() == null) {
            this.inner().properties()
                .recurrence()
                .schedule().withHours(new ArrayList<Integer>());
        }
        for (int hour : hours) {
            this.inner().properties().recurrence().schedule().hours().add(hour);
        }

        return this;
    }

    @Override
    public JobImpl endingNever() {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
            this.inner().properties()
                .withRecurrence(new JobRecurrence());
        }
        this.inner().properties()
            .recurrence()
            .withCount(null) // only count or endTime or none are valid combination
            .withEndTime(null);

        return this;
    }

    @Override
    public JobImpl endingBy(DateTime endTime) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
            this.inner().properties()
                .withRecurrence(new JobRecurrence());
        }
        this.inner().properties()
            .recurrence()
            .withCount(null) // only count or endTime or none are valid combination
            .withEndTime(endTime);

        return this;
    }

    @Override
    public JobImpl endingAfterOccurrence(int countTimes) {
        if (this.inner().properties() == null) {
            this.inner().withProperties(new JobPropertiesInner());
            this.inner().properties()
                .withRecurrence(new JobRecurrence());
        }
        this.inner().properties()
            .recurrence()
            .withEndTime(null) // only count or endTime or none are valid combination
            .withCount(countTimes);

        return this;
    }
}
