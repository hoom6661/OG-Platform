/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Test;

import com.opengamma.id.Identifier;

/**
 * Test PortfolioNodeImpl.
 */
public class PortfolioNodeImplTest {

  @Test
  public void test_construction() {
    PortfolioNodeImpl test = new PortfolioNodeImpl();
    assertEquals("", test.getName());
    assertEquals(0, test.getChildNodes().size());
    assertEquals(0, test.getPositions().size());
    assertEquals(0, test.size());
    assertEquals("PortfolioNode[, 0 child-nodes, 0 positions]", test.toString());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_construction_String() {
    PortfolioNodeImpl test = new PortfolioNodeImpl("Name");
    assertEquals("Name", test.getName());
    assertEquals(0, test.getChildNodes().size());
    assertEquals(0, test.getPositions().size());
    assertEquals(0, test.size());
    assertEquals("PortfolioNode[Name, 0 child-nodes, 0 positions]", test.toString());
  }

  @Test(expected=NullPointerException.class)
  public void test_construction_String_null() {
    new PortfolioImpl(null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_setName() {
    PortfolioNodeImpl test = new PortfolioNodeImpl("Name");
    assertEquals("Name", test.getName());
    test.setName("NewName");
    assertEquals("NewName", test.getName());
  }

  //-------------------------------------------------------------------------
  @Test(expected=UnsupportedOperationException.class)
  public void test_getChildNodes_immutable() {
    PortfolioNodeImpl test = new PortfolioNodeImpl();
    PortfolioNodeImpl child = new PortfolioNodeImpl();
    test.getChildNodes().add(child);
  }

  @Test
  public void test_addChildNode() {
    PortfolioNodeImpl test = new PortfolioNodeImpl();
    PortfolioNodeImpl child = new PortfolioNodeImpl();
    test.addChildNode(child);
    assertEquals(1, test.getChildNodes().size());
    assertEquals(child, test.getChildNodes().get(0));
    assertEquals(0, test.getPositions().size());
    assertEquals(1, test.size());
  }

  @Test
  public void test_addChildNodes() {
    PortfolioNodeImpl test = new PortfolioNodeImpl();
    PortfolioNodeImpl child0 = new PortfolioNodeImpl();
    PortfolioNodeImpl child1 = new PortfolioNodeImpl();
    test.addChildNodes(Arrays.asList(child0, child1));
    assertEquals(2, test.getChildNodes().size());
    assertEquals(child0, test.getChildNodes().get(0));
    assertEquals(child1, test.getChildNodes().get(1));
    assertEquals(0, test.getPositions().size());
    assertEquals(2, test.size());
  }

  @Test
  public void test_removeChildNode_match() {
    PortfolioNodeImpl test = new PortfolioNodeImpl();
    PortfolioNodeImpl child = new PortfolioNodeImpl();
    test.addChildNode(child);
    assertEquals(1, test.getChildNodes().size());
    test.removeChildNode(child);
    assertEquals(0, test.getChildNodes().size());
  }

  @Test
  public void test_removeChildNode_noMatch() {
    PortfolioNodeImpl test = new PortfolioNodeImpl();
    PortfolioNodeImpl child = new PortfolioNodeImpl();
    PortfolioNodeImpl removing = new PortfolioNodeImpl();
    test.addChildNode(child);
    assertEquals(1, test.getChildNodes().size());
    test.removeChildNode(removing);
    assertEquals(1, test.getChildNodes().size());
    assertEquals(child, test.getChildNodes().get(0));
  }

  //-------------------------------------------------------------------------
  @Test(expected=UnsupportedOperationException.class)
  public void test_getPositions_immutable() {
    PortfolioNodeImpl test = new PortfolioNodeImpl();
    PositionBean child = new PositionBean(BigDecimal.ONE, new Identifier("K", "V"));
    test.getPositions().add(child);
  }

  @Test
  public void test_addPosition() {
    PortfolioNodeImpl test = new PortfolioNodeImpl();
    PositionBean child = new PositionBean(BigDecimal.ONE, new Identifier("K", "V"));
    test.addPosition(child);
    assertEquals(1, test.getPositions().size());
    assertEquals(child, test.getPositions().get(0));
    assertEquals(0, test.getChildNodes().size());
    assertEquals(1, test.size());
  }

  @Test
  public void test_addPositions() {
    PortfolioNodeImpl test = new PortfolioNodeImpl();
    PositionBean child0 = new PositionBean(BigDecimal.ONE, new Identifier("K", "V"));
    PositionBean child1 = new PositionBean(BigDecimal.ONE, new Identifier("K", "V"));
    test.addPositions(Arrays.asList(child0, child1));
    assertEquals(2, test.getPositions().size());
    assertEquals(child0, test.getPositions().get(0));
    assertEquals(child1, test.getPositions().get(1));
    assertEquals(0, test.getChildNodes().size());
    assertEquals(2, test.size());
  }

  @Test
  public void test_removePosition_match() {
    PortfolioNodeImpl test = new PortfolioNodeImpl();
    PositionBean child = new PositionBean(BigDecimal.ONE, new Identifier("K", "V"));
    test.addPosition(child);
    assertEquals(1, test.getPositions().size());
    test.removePosition(child);
    assertEquals(0, test.getPositions().size());
  }

  @Test
  public void test_removePosition_noMatch() {
    PortfolioNodeImpl test = new PortfolioNodeImpl();
    PositionBean child = new PositionBean(BigDecimal.ONE, new Identifier("K", "V"));
    PositionBean removing = new PositionBean(BigDecimal.ONE, new Identifier("K", "OTHER"));
    test.addPosition(child);
    assertEquals(1, test.getPositions().size());
    test.removePosition(removing);
    assertEquals(1, test.getPositions().size());
    assertEquals(child, test.getPositions().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_size() {
    PortfolioNodeImpl test = new PortfolioNodeImpl();
    PortfolioNodeImpl child1 = new PortfolioNodeImpl();
    PositionBean child2 = new PositionBean(BigDecimal.ONE, new Identifier("K", "V"));
    test.addChildNode(child1);
    test.addPosition(child2);
    assertEquals(1, test.getChildNodes().size());
    assertEquals(1, test.getPositions().size());
    assertEquals(2, test.size());
  }

}
