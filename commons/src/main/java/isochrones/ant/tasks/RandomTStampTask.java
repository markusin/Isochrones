package isochrones.ant.tasks;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Property;

/**
 * <p>
 * The <code>RandomTStampTask</code> class
 * </p>
 * <p>
 * Copyright: 2006 - 2009 <a href="http://www.inf.unibz.it/dis">Dis Research Group</a>
 * </p>
 * <p>
 * Domenikanerplatz - Bozen, Italy.
 * </p>
 * <p>
 * </p>
 * 
 * @author <a href="mailto:markus.innerebner@inf.unibz.it">Markus Innerebner</a>.
 * @version 2.2
 */
public class RandomTStampTask extends Task {

  String property;
  String from, to;
  String pattern = "yyyy-MM-dd'T'HH:mm";
  SimpleDateFormat dateFormat;
  private boolean override = false;

  /**
   * <p>
   * Method validate
   * </p>
   */
  private void validate() {
    if (property == null)
      throw new BuildException("You must specify an attribute named property.");
    if (from == null)
      throw new BuildException("You must specify an attribute named from.");
    if (to == null)
      throw new BuildException("You must specify an attribute named to.");
  }

  @Override
  public void execute() {
    validate();

    Calendar fromDate = Calendar.getInstance();
    Calendar toDate = Calendar.getInstance();
    try {
      fromDate.setTimeInMillis(dateFormat.parse(from).getTime());
      toDate.setTimeInMillis(dateFormat.parse(to).getTime());
      
      //System.out.println("From time is: " + dateFormat.format(fromDate.getTime()));
      //System.out.println("To time is: " + dateFormat.format(toDate.getTime()));
      

      Calendar generated = Calendar.getInstance();
      generated.setTimeInMillis(fromDate.getTimeInMillis());
      generated.set(Calendar.HOUR_OF_DAY, 0);
      generated.set(Calendar.MINUTE, 0);
      generated.set(Calendar.SECOND, 0);
      generated.set(Calendar.MILLISECOND, 0);

      int fromInMinutesAfterMidnight = (int) ((fromDate.getTimeInMillis() - generated.getTimeInMillis()) /  60000);
      int toInMinutesAfterMidnight =  (int) ((toDate.getTimeInMillis() - generated.getTimeInMillis()) / 60000);
      int randomMinAfterMidnight = randomNumber(Math.round(fromInMinutesAfterMidnight), Math.round(toInMinutesAfterMidnight));
      //System.out.println("From time in mins: " + fromInMinutesAfterMidnight);
      //System.out.println("To time mins: " + toInMinutesAfterMidnight);
      //System.out.println("Generated time mins: " + randomMinAfterMidnight);
      generated.add(Calendar.MINUTE, randomMinAfterMidnight);
      setPropertyValue(dateFormat.format(generated.getTime()));
    } catch (ParseException e) {
      e.printStackTrace();
    }
  }

  /**
   * <p>
   * Method setOverride
   * </p>
   * specifies if a property should be overwritten. Default false
   * 
   * @param override
   */
  public void setOverride(boolean override) {
    this.override = override;
  }

  /**
   * <p>
   * Method setPropertyValue
   * </p>
   * 
   * @param value
   */
  private final void setPropertyValue(String value) {
    {
      if (override) {
        if (getProject().getUserProperty(property) == null)
          getProject().setProperty(property, value);
        else
          getProject().setUserProperty(property, value);
      } else {
        // Property p = (Property) project.createTask("property");
        Property p = (Property) getProject().createTask("property");
        p.setName(property);
        p.setValue(value);
        p.execute();
      }
    }
  }

  /**
   * <p>
   * Method randomNumber
   * </p>
   * 
   * @param min
   * @param max
   * @return
   */
  public static int randomNumber(int min, int max) {
    return min + (new Random()).nextInt(max - min);
  }

  /**
   * <p>
   * Method setFrom set the from interval
   * </p>
   * 
   * @param from the time interval in string format
   */
  public void setFrom(String from) {
    this.from = from;
  }

  /**
   * <p>
   * Method setTo set the to interval
   * </p>
   * 
   * @param to the time interval in string format
   */
  public void setTo(String to) {
    this.to = to;
  }

  /**
   * <p>
   * Method setPattern sets the date pattern. Pattern are the one used in Dateformat
   * </p>
   * 
   * @see SimpleDateFormat
   * @param pattern the specified patter
   */
  public void setPattern(String pattern) {
    this.dateFormat = new SimpleDateFormat(pattern);
  }

  /**
   * <p>
   * Method setProperty specifies the name of the property in which the result is written
   * </p>
   * 
   * @param property the name of the property in which generated timestamp is written
   */
  public void setProperty(String property) {
    this.property = property;
  }

  public void printProperty() {
    String value = getProject().getProperty(property);
    System.out.println("Property:" + property + " value:" + value);
  }

  /**
   * <p>
   * Method main
   * </p>
   * only for testing
   * 
   * @param args
   */
  public static void main(String[] args) {
    RandomTStampTask task = new RandomTStampTask();
    task.setFrom(args[0]);
    task.setTo(args[1]);
    task.setPattern(args[2]);
    task.setProperty("randomTimestamp");
    task.execute();
    task.printProperty();
  }

}
