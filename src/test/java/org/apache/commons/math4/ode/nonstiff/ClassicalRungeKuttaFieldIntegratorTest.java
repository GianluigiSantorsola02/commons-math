/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.math4.ode.nonstiff;


import java.lang.reflect.Array;

import org.apache.commons.math4.Field;
import org.apache.commons.math4.RealFieldElement;
import org.apache.commons.math4.exception.DimensionMismatchException;
import org.apache.commons.math4.exception.MaxCountExceededException;
import org.apache.commons.math4.exception.NoBracketingException;
import org.apache.commons.math4.exception.NumberIsTooSmallException;
import org.apache.commons.math4.ode.FieldExpandableODE;
import org.apache.commons.math4.ode.FieldFirstOrderDifferentialEquations;
import org.apache.commons.math4.ode.FieldFirstOrderIntegrator;
import org.apache.commons.math4.ode.FieldODEState;
import org.apache.commons.math4.ode.FieldODEStateAndDerivative;
import org.apache.commons.math4.ode.TestFieldProblem1;
import org.apache.commons.math4.ode.TestFieldProblem2;
import org.apache.commons.math4.ode.TestFieldProblem3;
import org.apache.commons.math4.ode.TestFieldProblem4;
import org.apache.commons.math4.ode.TestFieldProblem5;
import org.apache.commons.math4.ode.TestFieldProblem6;
import org.apache.commons.math4.ode.TestFieldProblemAbstract;
import org.apache.commons.math4.ode.TestFieldProblemHandler;
import org.apache.commons.math4.ode.events.Action;
import org.apache.commons.math4.ode.events.FieldEventHandler;
import org.apache.commons.math4.ode.sampling.FieldStepHandler;
import org.apache.commons.math4.ode.sampling.FieldStepInterpolator;
import org.apache.commons.math4.util.Decimal64Field;
import org.apache.commons.math4.util.FastMath;
import org.apache.commons.math4.util.MathArrays;
import org.junit.Assert;
import org.junit.Test;

public class ClassicalRungeKuttaFieldIntegratorTest {

      @Test
      public void testMissedEndEvent()
          throws DimensionMismatchException, NumberIsTooSmallException,
                 MaxCountExceededException, NoBracketingException {
          doTestMissedEndEvent(Decimal64Field.getInstance());
      }

      private <T extends RealFieldElement<T>>void doTestMissedEndEvent(final Field<T> field)
          throws DimensionMismatchException, NumberIsTooSmallException,
              MaxCountExceededException, NoBracketingException {
          final T   t0     = field.getZero().add(1878250320.0000029);
          final T   tEvent = field.getZero().add(1878250379.9999986);
          final T[] k      = MathArrays.buildArray(field, 3);
          k[0] = field.getZero().add(1.0e-4);
          k[1] = field.getZero().add(1.0e-5);
          k[2] = field.getZero().add(1.0e-6);
          FieldFirstOrderDifferentialEquations<T> ode = new FieldFirstOrderDifferentialEquations<T>() {

              public int getDimension() {
                  return k.length;
              }

              public void init(T t0, T[] y0, T t) {
              }

              public T[] computeDerivatives(T t, T[] y) {
                  T[] yDot = MathArrays.buildArray(field, k.length);
                  for (int i = 0; i < y.length; ++i) {
                      yDot[i] = k[i].multiply(y[i]);
                  }
                  return yDot;
              }
          };

          ClassicalRungeKuttaFieldIntegrator<T> integrator =
                          new ClassicalRungeKuttaFieldIntegrator<T>(field, field.getZero().add(60.0));

          T[] y0   = MathArrays.buildArray(field, k.length);
          for (int i = 0; i < y0.length; ++i) {
              y0[i] = field.getOne().add(i);
          }

          FieldODEStateAndDerivative<T> result = integrator.integrate(new FieldExpandableODE<T>(ode),
                                                                      new FieldODEState<T>(t0, y0),
                                                                      tEvent);
          Assert.assertEquals(tEvent.getReal(), result.getTime().getReal(), 5.0e-6);
          T[] y = result.getState();
          for (int i = 0; i < y.length; ++i) {
              Assert.assertEquals(y0[i].multiply(k[i].multiply(result.getTime().subtract(t0)).exp()).getReal(),
                                  y[i].getReal(),
                                  1.0e-9);
          }

          integrator.addEventHandler(new FieldEventHandler<T>() {

              public void init(FieldODEStateAndDerivative<T> state0, T t) {
              }

              public FieldODEState<T> resetState(FieldODEStateAndDerivative<T> state) {
                  return state;
              }

              public T g(FieldODEStateAndDerivative<T> state) {
                  return state.getTime().subtract(tEvent);
              }

              public Action eventOccurred(FieldODEStateAndDerivative<T> state, boolean increasing) {
                  Assert.assertEquals(tEvent.getReal(), state.getTime().getReal(), 5.0e-6);
                  return Action.CONTINUE;
              }
          }, Double.POSITIVE_INFINITY, 1.0e-20, 100);
          result = integrator.integrate(new FieldExpandableODE<T>(ode),
                                        new FieldODEState<T>(t0, y0),
                                        tEvent.add(120));
          Assert.assertEquals(tEvent.add(120).getReal(), result.getTime().getReal(), 5.0e-6);
          y = result.getState();
          for (int i = 0; i < y.length; ++i) {
              Assert.assertEquals(y0[i].multiply(k[i].multiply(result.getTime().subtract(t0)).exp()).getReal(),
                                  y[i].getReal(),
                                  1.0e-9);
          }

      }

      @Test
      public void testSanityChecks()
          throws DimensionMismatchException, NumberIsTooSmallException,
                 MaxCountExceededException, NoBracketingException {
          doTestSanityChecks(Decimal64Field.getInstance());
      }

      private <T extends RealFieldElement<T>>void doTestSanityChecks(Field<T> field)
          throws DimensionMismatchException, NumberIsTooSmallException,
                 MaxCountExceededException, NoBracketingException {
          ClassicalRungeKuttaFieldIntegrator<T> integrator =
                          new ClassicalRungeKuttaFieldIntegrator<T>(field, field.getZero().add(0.01));
          try  {
              TestFieldProblem1<T> pb = new TestFieldProblem1<T>(field);
              integrator.integrate(new FieldExpandableODE<>(pb),
                                   new FieldODEState<T>(field.getZero(), MathArrays.buildArray(field, pb.getDimension() + 10)),
                                   field.getOne());
              Assert.fail("an exception should have been thrown");
          } catch(DimensionMismatchException ie) {
          }
          try  {
              TestFieldProblem1<T> pb = new TestFieldProblem1<T>(field);
              integrator.integrate(new FieldExpandableODE<>(pb),
                                   new FieldODEState<T>(field.getZero(), MathArrays.buildArray(field, pb.getDimension())),
                                   field.getZero());
              Assert.fail("an exception should have been thrown");
          } catch(NumberIsTooSmallException ie) {
          }
      }

      @Test
      public void testDecreasingSteps()
          throws DimensionMismatchException, NumberIsTooSmallException,
                 MaxCountExceededException, NoBracketingException {
          doTestDecreasingSteps(Decimal64Field.getInstance());
      }

      private <T extends RealFieldElement<T>>void doTestDecreasingSteps(Field<T> field)
          throws DimensionMismatchException, NumberIsTooSmallException,
                 MaxCountExceededException, NoBracketingException {

          @SuppressWarnings("unchecked")
          TestFieldProblemAbstract<T>[] allProblems =
                          (TestFieldProblemAbstract<T>[]) Array.newInstance(TestFieldProblemAbstract.class, 6);
          allProblems[0] = new TestFieldProblem1<T>(field);
          allProblems[1] = new TestFieldProblem2<T>(field);
          allProblems[2] = new TestFieldProblem3<T>(field);
          allProblems[3] = new TestFieldProblem4<T>(field);
          allProblems[4] = new TestFieldProblem5<T>(field);
          allProblems[5] = new TestFieldProblem6<T>(field);
          for (TestFieldProblemAbstract<T> pb :  allProblems) {

              T previousValueError = null;
              T previousTimeError  = null;
              for (int i = 4; i < 10; ++i) {

                  T step = pb.getFinalTime().subtract(pb.getInitialState().getTime()).multiply(FastMath.pow(2.0, -i));

                  FieldFirstOrderIntegrator<T> integ = new ClassicalRungeKuttaFieldIntegrator<T>(field, step);
                  TestFieldProblemHandler<T> handler = new TestFieldProblemHandler<T>(pb, integ);
                  integ.addStepHandler(handler);
                  FieldEventHandler<T>[] functions = pb.getEventsHandlers();
                  for (int l = 0; l < functions.length; ++l) {
                      integ.addEventHandler(functions[l],
                                            Double.POSITIVE_INFINITY, 1.0e-6 * step.getReal(), 1000);
                  }
                  Assert.assertEquals(functions.length, integ.getEventHandlers().size());
                  FieldODEStateAndDerivative<T> stop = integ.integrate(new FieldExpandableODE<T>(pb),
                                                                       pb.getInitialState(),
                                                                       pb.getFinalTime());
                  if (functions.length == 0) {
                      Assert.assertEquals(pb.getFinalTime().getReal(), stop.getTime().getReal(), 1.0e-10);
                  }

                  T error = handler.getMaximalValueError();
                  if (i > 4) {
                      Assert.assertTrue(error.subtract(previousValueError.abs().multiply(1.01)).getReal() < 0);
                  }
                  previousValueError = error;

                  T timeError = handler.getMaximalTimeError();
                  if (i > 4) {
                      Assert.assertTrue(timeError.subtract(previousTimeError.abs()).getReal() <= 0);
                  }
                  previousTimeError = timeError;

                  integ.clearEventHandlers();
                  Assert.assertEquals(0, integ.getEventHandlers().size());
              }

          }

      }

      @Test
      public void testSmallStep()
           throws DimensionMismatchException, NumberIsTooSmallException,
                  MaxCountExceededException, NoBracketingException {
          doTestSmallStep(Decimal64Field.getInstance());
      }

      private <T extends RealFieldElement<T>>void doTestSmallStep(Field<T> field)
           throws DimensionMismatchException, NumberIsTooSmallException,
                  MaxCountExceededException, NoBracketingException {

          TestFieldProblem1<T> pb = new TestFieldProblem1<T>(field);
          T step = pb.getFinalTime().subtract(pb.getInitialState().getTime()).multiply(0.001);

          FieldFirstOrderIntegrator<T> integ = new ClassicalRungeKuttaFieldIntegrator<T>(field, step);
          TestFieldProblemHandler<T> handler = new TestFieldProblemHandler<T>(pb, integ);
          integ.addStepHandler(handler);
          integ.integrate(new FieldExpandableODE<T>(pb), pb.getInitialState(), pb.getFinalTime());

          Assert.assertTrue(handler.getLastError().getReal() < 2.0e-13);
          Assert.assertTrue(handler.getMaximalValueError().getReal() < 4.0e-12);
          Assert.assertEquals(0, handler.getMaximalTimeError().getReal(), 1.0e-12);
          Assert.assertEquals("classical Runge-Kutta", integ.getName());
      }

      @Test
      public void testBigStep()
          throws DimensionMismatchException, NumberIsTooSmallException,
                 MaxCountExceededException, NoBracketingException {
          doTestBigStep(Decimal64Field.getInstance());
      }

      private <T extends RealFieldElement<T>>void doTestBigStep(Field<T> field)
          throws DimensionMismatchException, NumberIsTooSmallException,
                 MaxCountExceededException, NoBracketingException {

          TestFieldProblem1<T> pb = new TestFieldProblem1<T>(field);
          T step = pb.getFinalTime().subtract(pb.getInitialState().getTime()).multiply(0.2);

          FieldFirstOrderIntegrator<T> integ = new ClassicalRungeKuttaFieldIntegrator<T>(field, step);
          TestFieldProblemHandler<T> handler = new TestFieldProblemHandler<T>(pb, integ);
          integ.addStepHandler(handler);
          integ.integrate(new FieldExpandableODE<T>(pb), pb.getInitialState(), pb.getFinalTime());

          Assert.assertTrue(handler.getLastError().getReal() > 0.0004);
          Assert.assertTrue(handler.getMaximalValueError().getReal() > 0.005);
          Assert.assertEquals(0, handler.getMaximalTimeError().getReal(), 1.0e-12);

      }

      @Test
      public void testBackward()
          throws DimensionMismatchException, NumberIsTooSmallException,
                 MaxCountExceededException, NoBracketingException {
          doTestBackward(Decimal64Field.getInstance());
      }

      private <T extends RealFieldElement<T>>void doTestBackward(Field<T> field)
          throws DimensionMismatchException, NumberIsTooSmallException,
                 MaxCountExceededException, NoBracketingException {

          TestFieldProblem5<T> pb = new TestFieldProblem5<T>(field);
          T step = pb.getFinalTime().subtract(pb.getInitialState().getTime()).multiply(0.001).abs();

          FieldFirstOrderIntegrator<T> integ = new ClassicalRungeKuttaFieldIntegrator<T>(field, step);
          TestFieldProblemHandler<T> handler = new TestFieldProblemHandler<T>(pb, integ);
          integ.addStepHandler(handler);
          integ.integrate(new FieldExpandableODE<>(pb), pb.getInitialState(), pb.getFinalTime());

          Assert.assertTrue(handler.getLastError().getReal() < 5.0e-10);
          Assert.assertTrue(handler.getMaximalValueError().getReal() < 7.0e-10);
          Assert.assertEquals(0, handler.getMaximalTimeError().getReal(), 1.0e-12);
          Assert.assertEquals("classical Runge-Kutta", integ.getName());
      }

      @Test
      public void testKepler()
          throws DimensionMismatchException, NumberIsTooSmallException,
                 MaxCountExceededException, NoBracketingException {
          doTestKepler(Decimal64Field.getInstance());
      }

      private <T extends RealFieldElement<T>>void doTestKepler(Field<T> field)
          throws DimensionMismatchException, NumberIsTooSmallException,
                 MaxCountExceededException, NoBracketingException {

          final TestFieldProblem3<T> pb  = new TestFieldProblem3<T>(field, field.getZero().add(0.9));
          T step = pb.getFinalTime().subtract(pb.getInitialState().getTime()).multiply(0.0003);

          FieldFirstOrderIntegrator<T> integ = new ClassicalRungeKuttaFieldIntegrator<T>(field, step);
          integ.addStepHandler(new KeplerHandler<T>(pb));
          integ.integrate(new FieldExpandableODE<>(pb), pb.getInitialState(), pb.getFinalTime());
      }

      private static class KeplerHandler<T extends RealFieldElement<T>> implements FieldStepHandler<T> {
          public KeplerHandler(TestFieldProblem3<T> pb) {
              this.pb = pb;
              maxError = pb.getField().getZero();
          }
          public void init(FieldODEStateAndDerivative<T> state0, T t) {
              maxError = pb.getField().getZero();
          }
          public void handleStep(FieldStepInterpolator<T> interpolator, boolean isLast)
                          throws MaxCountExceededException {

              FieldODEStateAndDerivative<T> current = interpolator.getCurrentState();
              T[] theoreticalY  = pb.computeTheoreticalState(current.getTime());
              T dx = current.getState()[0].subtract(theoreticalY[0]);
              T dy = current.getState()[1].subtract(theoreticalY[1]);
              T error = dx.multiply(dx).add(dy.multiply(dy));
              if (error.subtract(maxError).getReal() > 0) {
                  maxError = error;
              }
              if (isLast) {
                  // even with more than 1000 evaluations per period,
                  // RK4 is not able to integrate such an eccentric
                  // orbit with a good accuracy
                  Assert.assertTrue(maxError.getReal() > 0.005);
              }
          }
          private T maxError;
          private TestFieldProblem3<T> pb;
      }

      @Test
      public void testStepSize()
          throws DimensionMismatchException, NumberIsTooSmallException,
                 MaxCountExceededException, NoBracketingException {
          doTestStepSize(Decimal64Field.getInstance());
      }

      private <T extends RealFieldElement<T>>void doTestStepSize(Field<T> field)
          throws DimensionMismatchException, NumberIsTooSmallException,
                 MaxCountExceededException, NoBracketingException {
          final T step = field.getZero().add(1.23456);
          FieldFirstOrderIntegrator<T> integ = new ClassicalRungeKuttaFieldIntegrator<T>(field, step);
          integ.addStepHandler(new FieldStepHandler<T>() {
              public void handleStep(FieldStepInterpolator<T> interpolator, boolean isLast) {
                  if (! isLast) {
                      Assert.assertEquals(step.getReal(),
                                          interpolator.getCurrentState().getTime().subtract(interpolator.getPreviousState().getTime()).getReal(),
                                          1.0e-12);
                  }
              }
              public void init(FieldODEStateAndDerivative<T> s0, T t) {
              }
          });
          integ.integrate(new FieldExpandableODE<T>(new FieldFirstOrderDifferentialEquations<T>() {
              public void init(T t0, T[] y0, T t) {
              }
              public T[] computeDerivatives(T t, T[] y) {
                  T[] dot = MathArrays.buildArray(t.getField(), 1);
                  dot[0] = t.getField().getOne();
                  return dot;
              }
              public int getDimension() {
                  return 1;
              }
          }), new FieldODEState<T>(field.getZero(), MathArrays.buildArray(field, 1)), field.getZero().add(5.0));
      }

      @Test
      public void testTooLargeFirstStep() {
          doTestTooLargeFirstStep(Decimal64Field.getInstance());
      }

      private <T extends RealFieldElement<T>>void doTestTooLargeFirstStep(final Field<T> field) {

          RungeKuttaFieldIntegrator<T> integ = new ClassicalRungeKuttaFieldIntegrator<T>(field, field.getZero().add(0.5));
          final T t0 = field.getZero();
          final T[] y0 = MathArrays.buildArray(field, 1);
          y0[0] = field.getOne();
          final T t   = field.getZero().add(0.001);
          FieldFirstOrderDifferentialEquations<T> equations = new FieldFirstOrderDifferentialEquations<T>() {

              public int getDimension() {
                  return 1;
              }

              public void init(T t0, T[] y0, T t) {
              }

              public T[] computeDerivatives(T t, T[] y) {
                  Assert.assertTrue(t.getReal() >= FastMath.nextAfter(t0.getReal(), Double.NEGATIVE_INFINITY));
                  Assert.assertTrue(t.getReal() <= FastMath.nextAfter(t.getReal(),   Double.POSITIVE_INFINITY));
                  T[] yDot = MathArrays.buildArray(field, 1);
                  yDot[0] = y[0].multiply(-100.0);
                  return yDot;
              }

          };

          integ.integrate(new FieldExpandableODE<>(equations), new FieldODEState<T>(t0, y0), t);

      }

}
