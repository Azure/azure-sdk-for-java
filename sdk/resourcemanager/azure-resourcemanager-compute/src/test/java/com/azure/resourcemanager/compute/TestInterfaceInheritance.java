package com.azure.resourcemanager.compute;

/**
 * @author xiaofeicao
 * @createdAt 2021-11-30 14:29
 */
public class TestInterfaceInheritance {


    interface WithCreate{

    }

    interface WithApply {

    }

    interface DefinitionStages {


        interface Blank<ParentT> {
        }

        interface WithFlexibleProfile{
            Blank<WithCreate> define();
        }
    }

    interface UpdateStages {

        interface Blank<ParentT> {
        }

        interface WithFlexibleProfile{
            Blank<WithApply> define();
        }
    }

    interface ParentWithApply extends UpdateStages.Blank<WithApply> {

    }

    interface BlankWithCreate extends DefinitionStages.Blank<WithCreate> {

    }
    
    public static class Implementation implements ParentWithApply, BlankWithCreate, WithCreate, WithApply, DefinitionStages.WithFlexibleProfile, UpdateStages.WithFlexibleProfile {

        @Override
        public Implementation define() {
            return new Implementation();
        }
    }

    public static void main(String[] args) {
        Implementation i = new Implementation();
        i.define();
    }
    
}

