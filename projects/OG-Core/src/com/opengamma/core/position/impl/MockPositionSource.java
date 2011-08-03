/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.PositionSource;
import com.opengamma.id.IdUtils;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;
import com.opengamma.util.ArgumentChecker;

/**
 * A simple mutable implementation of a source of positions.
 * <p>
 * This class is intended for testing scenarios.
 * It is not thread-safe and must not be used in production.
 */
public class MockPositionSource implements PositionSource {
  // this is currently public for indirect use by another project via ViewTestUtils

  /**
   * The portfolios.
   */
  private final Map<UniqueId, Portfolio> _portfolios = new ConcurrentHashMap<UniqueId, Portfolio>();
  /**
   * A cache of nodes by identifier.
   */
  private final Map<UniqueId, PortfolioNode> _nodes = new ConcurrentHashMap<UniqueId, PortfolioNode>();
  /**
   * A cache of positions by identifier.
   */
  private final Map<UniqueId, Position> _positions = new ConcurrentHashMap<UniqueId, Position>();
  /**
   * A cache of trades by identifier.
   */
  private final Map<UniqueId, Trade> _trades = new ConcurrentHashMap<UniqueId, Trade>();
  /**
   * The suppler of unique identifiers.
   */
  private final UniqueIdSupplier _uniqueIdSupplier;

  /**
   * Creates an instance using the default scheme for each {@link UniqueId} created.
   */
  public MockPositionSource() {
    _uniqueIdSupplier = new UniqueIdSupplier("Mock");
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the list of all portfolio identifiers.
   * 
   * @return the portfolio identifiers, unmodifiable, not null
   */
  public Set<UniqueId> getPortfolioIds() {
    return _portfolios.keySet();
  }

  /**
   * Gets a specific root portfolio by name.
   * 
   * @param identifier  the identifier, null returns null
   * @return the portfolio, null if not found
   */
  public Portfolio getPortfolio(UniqueId identifier) {
    return identifier == null ? null : _portfolios.get(identifier);
  }

  /**
   * Finds a specific node from any portfolio by identifier.
   * 
   * @param identifier  the identifier, null returns null
   * @return the node, null if not found
   */
  public PortfolioNode getPortfolioNode(UniqueId identifier) {
    return identifier == null ? null : _nodes.get(identifier);
  }

  /**
   * Finds a specific position from any portfolio by identifier.
   * 
   * @param identifier  the identifier, null returns null
   * @return the position, null if not found
   */
  public Position getPosition(UniqueId identifier) {
    return identifier == null ? null : _positions.get(identifier);
  }

  /**
   * Finds a specific trade from any portfolio by identifier.
   * 
   * @param uniqueId  the identifier, null returns null
   * @return the trade, null if not found
   */
  @Override
  public Trade getTrade(UniqueId uniqueId) {
    return uniqueId == null ? null : _trades.get(uniqueId);
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a portfolio to the master.
   * 
   * @param portfolio  the portfolio to add, not null
   */
  public void addPortfolio(Portfolio portfolio) {
    ArgumentChecker.notNull(portfolio, "portfolio");

    _portfolios.put(portfolio.getUniqueId(), portfolio);
    addToCache(portfolio.getUniqueId().getValue(), null, portfolio.getRootNode());
  }

  /**
   * Adds a node to the cache.
   * 
   * @param portfolioId  the id, not null
   * @param node  the node to add, not null
   */
  private void addToCache(String portfolioId, UniqueId parentNode, PortfolioNode node) {
    // node
    if (node instanceof PortfolioNodeImpl) {
      PortfolioNodeImpl nodeImpl = (PortfolioNodeImpl) node;
      nodeImpl.setUniqueId(_uniqueIdSupplier.getWithValuePrefix(portfolioId + "-"));
      nodeImpl.setParentNodeId(parentNode);
    }
    _nodes.put(node.getUniqueId(), node);
    
    // position
    for (Position position : node.getPositions()) {
      if (position instanceof PositionImpl) {
        PositionImpl positionImpl = (PositionImpl) position;
        positionImpl.setUniqueId(_uniqueIdSupplier.getWithValuePrefix(portfolioId + "-"));
        positionImpl.setParentNodeId(node.getUniqueId());
        
        //add trades
        for (Trade trade : positionImpl.getTrades()) {
          IdUtils.setInto(trade, _uniqueIdSupplier.getWithValuePrefix(portfolioId + "-"));
          _trades.put(trade.getUniqueId(), trade);
        }
      }
      _positions.put(position.getUniqueId(), position);
    }
    
    // recurse
    for (PortfolioNode child : node.getChildNodes()) {
      addToCache(portfolioId, node.getUniqueId(), child);
    }
  }

  /**
   * Removes a portfolio from the master.
   * 
   * @param portfolio  the portfolio to remove, not null
   */
  public void removePortfolio(Portfolio portfolio) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    _portfolios.remove(portfolio.getUniqueId());
    removeFromCache(portfolio.getRootNode());
  }

  /**
   * Removes a node from the cache
   * 
   * @param node  the node to remove, not null
   */
  private void removeFromCache(PortfolioNode node) {
    _nodes.remove(node.getUniqueId());
    for (Position position : node.getPositions()) {
      for (Trade trade : position.getTrades()) {
        _trades.remove(trade.getUniqueId());
      }
      _positions.remove(position.getUniqueId());
    }
    for (PortfolioNode child : node.getChildNodes()) {
      removeFromCache(child);
    }
  }

}
