package com.scalar.database.storage.cassandra;

import com.scalar.database.api.Delete;
import com.scalar.database.api.Get;
import com.scalar.database.api.MutationCondition;
import com.scalar.database.api.Operation;
import com.scalar.database.api.Put;
import com.scalar.database.api.PutIf;
import com.scalar.database.api.PutIfExists;
import com.scalar.database.api.Scan;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * A manager for all the statements
 *
 * @author Hiroyui Yamada
 */
@Immutable
public class StatementHandlerManager {
  private final SelectStatementHandler select;
  private final InsertStatementHandler insert;
  private final UpdateStatementHandler update;
  private final DeleteStatementHandler delete;

  private StatementHandlerManager(Builder builder) {
    this.select = builder.select;
    this.insert = builder.insert;
    this.update = builder.update;
    this.delete = builder.delete;
  }

  @Nonnull
  public SelectStatementHandler select() {
    return select;
  }

  @Nonnull
  public InsertStatementHandler insert() {
    return insert;
  }

  @Nonnull
  public UpdateStatementHandler update() {
    return update;
  }

  @Nonnull
  public DeleteStatementHandler delete() {
    return delete;
  }

  @Nonnull
  public StatementHandler get(Operation operation) {
    if (operation instanceof Get || operation instanceof Scan) {
      return select();
    } else if (operation instanceof Put) {
      MutationCondition condition = ((Put) operation).getCondition().orElse(null);
      if (condition != null && (condition instanceof PutIf || condition instanceof PutIfExists)) {
        return update();
      } else {
        return insert();
      }
    } else if (operation instanceof Delete) {
      return delete();
    }
    // never comes here usually
    throw new IllegalArgumentException("unexpected operation was given.");
  }

  @Nonnull
  public static StatementHandlerManager.Builder builder() {
    return new StatementHandlerManager.Builder();
  }

  public static class Builder {
    private SelectStatementHandler select;
    private InsertStatementHandler insert;
    private UpdateStatementHandler update;
    private DeleteStatementHandler delete;

    public Builder select(SelectStatementHandler select) {
      this.select = select;
      return this;
    }

    public Builder insert(InsertStatementHandler insert) {
      this.insert = insert;
      return this;
    }

    public Builder update(UpdateStatementHandler update) {
      this.update = update;
      return this;
    }

    public Builder delete(DeleteStatementHandler delete) {
      this.delete = delete;
      return this;
    }

    public StatementHandlerManager build() {
      if (select == null || insert == null || update == null || delete == null) {
        throw new IllegalArgumentException("please set all the statement handlers.");
      }
      return new StatementHandlerManager(this);
    }
  }
}
