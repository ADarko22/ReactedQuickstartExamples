package adarko22.reacted.quickstart.vaadin.view;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class MainGUI extends UI {

  @Override
  protected void init(VaadinRequest vaadinRequest) {
    // The root of the component hierarchy
    VerticalLayout content = new VerticalLayout();
    content.setSizeFull(); // Use entire window
    setContent(content);   // Attach to the UI
  }

  // todo find a way to use ReActors to communicate with gui
}
