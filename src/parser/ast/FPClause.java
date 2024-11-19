package parser.ast;

import parser.ast.FPBody;
import parser.ast.FPHead;

public class FPClause {
  public final FPHead head;
  public final FPBody body; 
  public final boolean isfact;
  public final boolean isrule;

  public FPClause(FPHead hd ) {
    
    head = hd;
    body = null;
    isfact = true;
    isrule = false;
  }

  public FPClause(FPHead hd, FPBody bd) {
    head = hd;
    body = bd;
    isfact = false;
    isrule = true;

  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (head != null) {
      sb.append(head.toString());
    } else {
      sb.append("?- ");
    }
    if (head != null && body != null) {
      sb.append(" :- ");
    }
    if (body != null) { 
      sb.append(body.toString());
    };
    return sb.append(".").toString();
  }

  public FPBody getBody() {
    return body;
  }

public FPHead getHead() {
    return head;
}

public boolean isFact() {
  // A fact has no body and is explicitly marked as isfact = true
  return isfact && body == null;
}

public boolean isRule() {
  // A rule has both a head and a body, and is explicitly marked as isrule = true
  return isrule && body != null;
}

public boolean isQuery() {
  // A query has no head but may have a body
  return head == null;
}
}