/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.time.Instant;
import javax.time.calendar.LocalDate;
import javax.time.calendar.OffsetTime;
import javax.time.calendar.ZonedDateTime;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.test.TestDependencyGraphExecutor;
import com.opengamma.engine.view.ResultModelDefinition;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.engine.view.calc.BatchExecutor;
import com.opengamma.engine.view.calc.DependencyGraphExecutor;
import com.opengamma.engine.view.calc.DependencyGraphExecutorFactory;
import com.opengamma.engine.view.calc.SingleComputationCycle;
import com.opengamma.engine.view.calc.SingleNodeExecutor;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.financial.batch.BatchDbManager;
import com.opengamma.financial.batch.BatchJob;
import com.opengamma.financial.batch.BatchJobRun;
import com.opengamma.financial.batch.LiveDataValue;
import com.opengamma.financial.batch.SnapshotId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.InetAddressUtils;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbSource;

/**
 * This implementation uses Hibernate to write all static data, including LiveData snapshots.
 * <p>
 * Risk itself is written using direct JDBC, however.
 */
public class BatchDbManagerImpl implements BatchDbManager {
  
  private static final Logger s_logger = LoggerFactory
    .getLogger(BatchDbManagerImpl.class);
  
  private DbSource _dbSource;
  private HibernateTemplate _hibernateTemplate;
  
  // --------------------------------------------------------------------------
  
  public PlatformTransactionManager getTransactionManager() {
    return _dbSource.getTransactionManager();
  }

  public SessionFactory getSessionFactory() {
    return _dbSource.getHibernateSessionFactory();
  }
  
  public HibernateTemplate getHibernateTemplate() {
    return _hibernateTemplate;
  }
  
  public SimpleJdbcTemplate getJdbcTemplate() {
    return _dbSource.getJdbcTemplate();
  }
  
  public void setDbSource(DbSource dbSource) {
    ArgumentChecker.notNull(dbSource, "dbSource");
    _dbSource = dbSource;    
    _hibernateTemplate = new HibernateTemplate(_dbSource.getHibernateSessionFactory());
    _hibernateTemplate.setAllowCreate(false);
  }
  
  // --------------------------------------------------------------------------
  
  /*package*/ OpenGammaVersion getOpenGammaVersion(final BatchJob job) {
    OpenGammaVersion version = getHibernateTemplate().execute(new HibernateCallback<OpenGammaVersion>() {
      @Override
      public OpenGammaVersion doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("OpenGammaVersion.one.byVersionAndHash");
        query.setString("version", job.getOpenGammaVersion());
        query.setString("hash", job.getOpenGammaVersionHash());
        return (OpenGammaVersion) query.uniqueResult();
      }
    });
    if (version == null) {
      version = new OpenGammaVersion();
      version.setVersion(job.getOpenGammaVersion());
      version.setHash(job.getOpenGammaVersionHash());
      getHibernateTemplate().save(version);
    }
    return version;
  }
  
  
  /*package*/ ObservationTime getObservationTime(final BatchJobRun job) {
    return getObservationTime(job.getObservationTime());
  }
  
  /*package*/ ObservationTime getObservationTime(final String label) {
    ObservationTime observationTime = getHibernateTemplate().execute(new HibernateCallback<ObservationTime>() {
      @Override
      public ObservationTime doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("ObservationTime.one.byLabel");
        query.setString("label", label);
        return (ObservationTime) query.uniqueResult();
      }
    });
    if (observationTime == null) {
      observationTime = new ObservationTime();
      observationTime.setLabel(label);
      getHibernateTemplate().save(observationTime);
    }
    return observationTime;
  }
 
  
  /*package*/ ObservationDateTime getObservationDateTime(final BatchJobRun job) {
    return getObservationDateTime(job.getObservationDate(), job.getObservationTime());     
  }
  
  /*package*/ ObservationDateTime getObservationDateTime(final LocalDate observationDate, final String observationTime) {
    ObservationDateTime dateTime = getHibernateTemplate().execute(new HibernateCallback<ObservationDateTime>() {
      @Override
      public ObservationDateTime doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("ObservationDateTime.one.byDateAndTime");
        query.setDate("date", DbDateUtils.toSqlDate(observationDate));
        query.setString("time", observationTime);
        return (ObservationDateTime) query.uniqueResult();
      }
    });
    if (dateTime == null) {
      dateTime = new ObservationDateTime();
      dateTime.setDate(DbDateUtils.toSqlDate(observationDate));
      dateTime.setObservationTime(getObservationTime(observationTime));
      getHibernateTemplate().save(dateTime);
    }
    return dateTime;
  }
  
  /*package*/ ComputeHost getComputeHost(final String hostName) {
    ComputeHost computeHost = getHibernateTemplate().execute(new HibernateCallback<ComputeHost>() {
      @Override
      public ComputeHost doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("ComputeHost.one.byHostName");
        query.setString("hostName", hostName);
        return (ComputeHost) query.uniqueResult();
      }
    });
    if (computeHost == null) {
      computeHost = new ComputeHost();
      computeHost.setHostName(hostName);
      getHibernateTemplate().save(computeHost);
    }
    return computeHost;
  }
 
  /*package*/ ComputeHost getLocalComputeHost() {
    return getComputeHost(InetAddressUtils.getLocalHostName());
  }
  
  /*package*/ ComputeNode getComputeNode(String nodeId) {
    String hostName = nodeId; 
    int slashIndex = nodeId.indexOf('/'); // e.g., mymachine-t5500/0/1, see LocalCalculationNode.java. Should refactor nodeId to a class with two strings, host and node id
    if (slashIndex != -1) {
      hostName = nodeId.substring(0, slashIndex);      
    }
    final ComputeHost host = getComputeHost(hostName);
    
    ComputeNode node = getHibernateTemplate().execute(new HibernateCallback<ComputeNode>() {
      @Override
      public ComputeNode doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("ComputeNode.one.byHostName");
        query.setString("hostName", host.getHostName());
        return (ComputeNode) query.uniqueResult();
      }
    });
    if (node == null) {
      node = new ComputeNode();
      node.setComputeHost(host);
      node.setConfigOid("UNDEFINED"); // TODO
      node.setConfigVersion(1); // TODO
      node.setNodeName(host.getHostName());
      getHibernateTemplate().save(node);
    }
    return node;
  }
  
  /*package*/ ComputeNode getLocalComputeNode() {
    return getComputeNode(InetAddressUtils.getLocalHostName());
  }
  
  /*package*/ LiveDataSnapshot getLiveDataSnapshot(final BatchJobRun job) {

    LiveDataSnapshot liveDataSnapshot = getLiveDataSnapshot(
        job.getSnapshotObservationDate(),
        job.getSnapshotObservationTime());
    
    if (liveDataSnapshot == null) {
      throw new IllegalArgumentException("Snapshot for " 
          + job.getSnapshotObservationDate() 
          + "/" 
          + job.getSnapshotObservationTime() 
          + " cannot be found");
    }
    
    return liveDataSnapshot;
  }
  
  /*package*/ LiveDataSnapshot getLiveDataSnapshot(final LocalDate observationDate, final String observationTime) {
    LiveDataSnapshot liveDataSnapshot = getHibernateTemplate().execute(new HibernateCallback<LiveDataSnapshot>() {
      @Override
      public LiveDataSnapshot doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("LiveDataSnapshot.one.byDateAndTime");
        query.setDate("date", DbDateUtils.toSqlDate(observationDate));
        query.setString("time", observationTime);
        return (LiveDataSnapshot) query.uniqueResult();
      }
    });
    return liveDataSnapshot;
  }
  
  /*package*/ LiveDataField getLiveDataField(final String fieldName) {
    LiveDataField field = getHibernateTemplate().execute(new HibernateCallback<LiveDataField>() {
      @Override
      public LiveDataField doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("LiveDataField.one.byName");
        query.setString("name", fieldName);
        return (LiveDataField) query.uniqueResult();
      }
    });
    if (field == null) {
      field = new LiveDataField();
      field.setName(fieldName);
      getHibernateTemplate().save(field);
    }
    return field;
  }
  
  /*package*/ ComputationTarget getComputationTarget(final ComputationTargetSpecification spec) {
    ComputationTarget computationTarget = getHibernateTemplate().execute(new HibernateCallback<ComputationTarget>() {
      @Override
      public ComputationTarget doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("ComputationTarget.one.byTypeAndId");
        query.setInteger("computationTargetType", ComputationTarget.getType(spec.getType()));
        query.setString("idScheme", spec.getIdentifier().getScheme().getName());
        query.setString("idValue", spec.getIdentifier().getValue());
        return (ComputationTarget) query.uniqueResult();
      }
    });
    if (computationTarget == null) {
      computationTarget = new ComputationTarget();
      computationTarget.setComputationTargetType(spec.getType());
      computationTarget.setIdScheme(spec.getIdentifier().getScheme().getName());      
      computationTarget.setIdValue(spec.getIdentifier().getValue());
      getHibernateTemplate().save(computationTarget);
    }
    return computationTarget;
  }
  
  /*package*/ RiskValueName getRiskValueName(final String name) {
    RiskValueName riskValueName = getHibernateTemplate().execute(new HibernateCallback<RiskValueName>() {
      @Override
      public RiskValueName doInHibernate(Session session) throws HibernateException,
          SQLException {
        Query query = session.getNamedQuery("RiskValueName.one.byName");
        query.setString("name", name);
        return (RiskValueName) query.uniqueResult();
      }
    });
    if (riskValueName == null) {
      riskValueName = new RiskValueName();
      riskValueName.setName(name);
      getHibernateTemplate().save(riskValueName);
    }
    return riskValueName;
  }
  
  /*package*/ RiskRun getRiskRunFromDb(final BatchJobRun job) {
    RiskRun riskRun = null;
    
    if (job.isForceNewRun() == false) {
      riskRun = getHibernateTemplate().execute(new HibernateCallback<RiskRun>() {
        @Override
        public RiskRun doInHibernate(Session session) throws HibernateException,
            SQLException {
          Query query = session.getNamedQuery("RiskRun.one.byViewAndRunTime");
          query.setString("viewOid", job.getViewOid());
          query.setInteger("viewVersion", job.getViewVersion());
          query.setDate("runDate", DbDateUtils.toSqlDate(job.getObservationDate()));
          query.setString("runTime", job.getObservationTime());
          return (RiskRun) query.uniqueResult();
        }
      });
      
      // here, if riskRun != null, we should check a hash of the configuration to see it's still the same as before  
    } 
    
    return riskRun;
  }
  
  /*package*/ RiskRun createRiskRun(final BatchJobRun job) {
    ZonedDateTime now = job.getCreationTime();
    
    LiveDataSnapshot snapshot = getLiveDataSnapshot(job);
    
    RiskRun riskRun = new RiskRun();
    riskRun.setOpenGammaVersion(getOpenGammaVersion(job.getJob()));
    riskRun.setMasterProcessHost(getLocalComputeHost());
    riskRun.setRunReason(job.getRunReason());
    riskRun.setRunTime(getObservationDateTime(job));
    riskRun.setValuationTime(DbDateUtils.toSqlTimestamp(job.getValuationTime()));
    riskRun.setViewOid(job.getViewOid());
    riskRun.setViewVersion(job.getViewVersion());
    riskRun.setLiveDataSnapshot(snapshot);
    riskRun.setCreateInstant(DbDateUtils.toSqlTimestamp(now));
    riskRun.setStartInstant(DbDateUtils.toSqlTimestamp(now));
    riskRun.setNumRestarts(0);
    riskRun.setComplete(false);
    
    for (ViewCalculationConfiguration calcConf : job.getCalculationConfigurations()) {
      riskRun.addCalculationConfiguration(calcConf);
    }
    
    getHibernateTemplate().save(riskRun);
    getHibernateTemplate().saveOrUpdateAll(riskRun.getCalculationConfigurations());
    return riskRun;
  }
  
  /*package*/ void restartRun(RiskRun riskRun) {
    Instant now = Instant.nowSystemClock();
    
    riskRun.setStartInstant(DbDateUtils.toSqlTimestamp(now));
    riskRun.setNumRestarts(riskRun.getNumRestarts() + 1);
    riskRun.setComplete(false);
    
    getHibernateTemplate().update(riskRun);
    
    // clear risk failures
    MapSqlParameterSource parameters = new MapSqlParameterSource()
      .addValue("run_id", riskRun.getId());
    getJdbcTemplate().update(RiskFailure.sqlDeleteRiskFailures(), parameters);
  }
  
  /*package*/ void endRun(RiskRun riskRun) {
    Instant now = Instant.nowSystemClock();
    
    riskRun.setEndInstant(DbDateUtils.toSqlTimestamp(now));
    riskRun.setComplete(true);
    
    getHibernateTemplate().update(riskRun);
  }
  
  /*package*/ RiskRun getRiskRunFromHandle(BatchJobRun job) {
    return getDbHandle(job)._riskRun;
  }
  
  private DbHandle getDbHandle(BatchJobRun job) {
    Object handle = job.getDbHandle();
    if (handle == null) {
      throw new IllegalStateException("Job db handle is null");
    }
    if (!(handle instanceof DbHandle)) {
      throw new IllegalStateException("Job db handle must be of type DbHandle, was " + handle.getClass());
    }
    return (DbHandle) handle;
  }
  
  /*package*/ Set<RiskValueName> populateRiskValueNames(BatchJobRun job) {
    Set<RiskValueName> returnValue = new HashSet<RiskValueName>();
    
    Set<String> riskValueNames = job.getView().getViewEvaluationModel().getAllOutputValueNames();
    for (String name : riskValueNames) {
      RiskValueName riskValueName = getRiskValueName(name);
      returnValue.add(riskValueName);
    }
    
    return returnValue;
  }
  
  /*package*/ Set<ComputationTarget> populateComputationTargets(BatchJobRun job) {
    Set<ComputationTarget> returnValue = new HashSet<ComputationTarget>();
    
    Set<ComputationTargetSpecification> computationTargets = job.getView().getViewEvaluationModel().getAllComputationTargets();
    for (ComputationTargetSpecification ct : computationTargets) {
      ComputationTarget computationTarget = getComputationTarget(ct);
      returnValue.add(computationTarget);
    }
    
    return returnValue;
  }
  
  // --------------------------------------------------------------------------
  

  @Override
  public void addValuesToSnapshot(SnapshotId snapshotId, Set<LiveDataValue> values) {
    s_logger.info("Adding {} values to LiveData snapshot {}", values.size(), snapshotId);
    
    try {
      getSessionFactory().getCurrentSession().beginTransaction();
      
      LiveDataSnapshot snapshot = getLiveDataSnapshot(snapshotId.getObservationDate(), snapshotId.getObservationTime());
      if (snapshot == null) {
        throw new IllegalArgumentException("Snapshot " + snapshotId + " cannot be found");
      }
      
      Collection<LiveDataSnapshotEntry> changedEntries = new ArrayList<LiveDataSnapshotEntry>();
      for (LiveDataValue value : values) {
        LiveDataSnapshotEntry entry = snapshot.getEntry(value.getComputationTargetSpecification(), value.getFieldName());
        if (entry != null) {
          if (entry.getValue() != value.getValue()) {
            entry.setValue(value.getValue());
            changedEntries.add(entry);
          }
        } else {
          entry = new LiveDataSnapshotEntry();
          entry.setSnapshot(snapshot);
          entry.setComputationTarget(getComputationTarget(value.getComputationTargetSpecification()));
          entry.setField(getLiveDataField(value.getFieldName()));
          entry.setValue(value.getValue());
          snapshot.addEntry(entry);
          changedEntries.add(entry);
        }
      }
      
      getHibernateTemplate().saveOrUpdateAll(changedEntries);
      
      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }

  @Override
  public void createLiveDataSnapshot(SnapshotId snapshotId) {
    s_logger.info("Creating LiveData snapshot {} ", snapshotId);
    
    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      LiveDataSnapshot snapshot = getLiveDataSnapshot(snapshotId.getObservationDate(), snapshotId.getObservationTime());
      if (snapshot != null) {
        s_logger.info("Snapshot " + snapshotId + " already exists. No need to create.");
        return;
      }
      
      snapshot = new LiveDataSnapshot();
      
      ObservationDateTime snapshotTime = getObservationDateTime(
          snapshotId.getObservationDate(), 
          snapshotId.getObservationTime());
      snapshot.setSnapshotTime(snapshotTime);
      
      getHibernateTemplate().save(snapshot);
      
      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }

  @Override
  public void endBatch(BatchJobRun batch) {
    s_logger.info("Ending batch {}", batch);
    
    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      RiskRun run = getRiskRunFromHandle(batch);
      endRun(run);
    
      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }

  @Override
  public void fixLiveDataSnapshotTime(SnapshotId snapshotId, OffsetTime fix) {
    s_logger.info("Fixing LiveData snapshot {} at {}", snapshotId, fix);
    
    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      LiveDataSnapshot snapshot = getLiveDataSnapshot(snapshotId.getObservationDate(), snapshotId.getObservationTime());
      
      if (snapshot == null) {
        throw new IllegalArgumentException("Snapshot " + snapshotId + " cannot be found");
      }
      
      snapshot.getSnapshotTime().setTime(DbDateUtils.toSqlTime(fix));
      getHibernateTemplate().save(snapshot);
      
      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }

  @Override
  public Set<LiveDataValue> getSnapshotValues(SnapshotId snapshotId) {
    s_logger.info("Getting LiveData snapshot {}", snapshotId);
    
    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      LiveDataSnapshot liveDataSnapshot = getLiveDataSnapshot(
          snapshotId.getObservationDate(), 
          snapshotId.getObservationTime());
      
      if (liveDataSnapshot == null) {
        throw new IllegalArgumentException("Snapshot " + snapshotId + " cannot be found");
      }
      
      Set<LiveDataValue> returnValues = new HashSet<LiveDataValue>();
      for (LiveDataSnapshotEntry entry : liveDataSnapshot.getSnapshotEntries()) {
        returnValues.add(entry.toLiveDataValue());      
      }
      
      getSessionFactory().getCurrentSession().getTransaction().commit();
      return returnValues;
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }

  @Override
  public void startBatch(BatchJobRun batch) {
    s_logger.info("Starting batch {}", batch);
    
    try {
      getSessionFactory().getCurrentSession().beginTransaction();

      RiskRun run = getRiskRunFromDb(batch);
      if (run == null) {
        run = createRiskRun(batch);
      } else {
        restartRun(run);
      }

      // make sure calc conf collection is inited
      for (CalculationConfiguration cc : run.getCalculationConfigurations()) { 
        ;
      }
      
      Set<RiskValueName> riskValueNames = populateRiskValueNames(batch);
      Set<ComputationTarget> computationTargets = populateComputationTargets(batch);
      
      DbHandle dbHandle = new DbHandle();
      dbHandle._riskRun = run;
      dbHandle._riskValueNames = riskValueNames;
      dbHandle._computationTargets = computationTargets;
      
      batch.setDbHandle(dbHandle);
      
      getSessionFactory().getCurrentSession().getTransaction().commit();
    } catch (RuntimeException e) {
      getSessionFactory().getCurrentSession().getTransaction().rollback();
      throw e;
    }
  }
  
  private static class DbHandle {
    private RiskRun _riskRun;
    private Set<RiskValueName> _riskValueNames;
    private Set<ComputationTarget> _computationTargets;
  }
  
  @Override
  public DependencyGraphExecutorFactory<Object> createDependencyGraphExecutorFactory(BatchJobRun batch) {
    return new BatchResultWriterFactory(batch);
  }
  
  public BatchResultWriter createTestResultWriter(BatchJobRun batch) {
    BatchResultWriterFactory factory = new BatchResultWriterFactory(batch);
    return factory.createTestWriter();    
  }
  
  private class BatchResultWriterFactory implements DependencyGraphExecutorFactory<Object> {
    
    private final BatchJobRun _batch;
    
    public BatchResultWriterFactory(BatchJobRun batch) {
      ArgumentChecker.notNull(batch, "batch");
      _batch = batch;
    }
    
    @Override
    public BatchExecutor createExecutor(SingleComputationCycle cycle) {
      DependencyGraphExecutor<CalculationJobResult> delegate =
        new SingleNodeExecutor(cycle);
      
      Map<String, ViewComputationCache> cachesByCalculationConfiguration = cycle.getCachesByCalculationConfiguration();
      
      BatchResultWriter resultWriter = new BatchResultWriter(
          _dbSource,
          delegate,
          cycle.getViewDefinition().getResultModelDefinition(),
          cachesByCalculationConfiguration,
          getDbHandle(_batch)._computationTargets,
          getRiskRunFromHandle(_batch),
          getDbHandle(_batch)._riskValueNames);
      
      return new BatchExecutor(resultWriter);
    }
    
    public BatchResultWriter createTestWriter() {
      DependencyGraphExecutor<CalculationJobResult> delegate = 
        new TestDependencyGraphExecutor<CalculationJobResult>(null);
      
      BatchResultWriter resultWriter = new BatchResultWriter(
          _dbSource,
          delegate,
          new ResultModelDefinition(),
          new HashMap<String, ViewComputationCache>(),
          getDbHandle(_batch)._computationTargets,
          getRiskRunFromHandle(_batch),
          getDbHandle(_batch)._riskValueNames);
      
      return resultWriter;
    }

  }
  
  public static Class<?>[] getHibernateMappingClasses() {
    return new HibernateBatchDbFiles().getHibernateMappingFiles();
  }
  
}
