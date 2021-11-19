#include "../include/Customer.h"

#include <iostream>
using namespace std;



//Customer ---------------------------------
Customer::Customer(std::string c_name, int c_id):name(c_name),id(c_id){};

//order and toString are virtual therefore i don't need to implement them here
std::string Customer::getName() const {
    return name;
}
int Customer::getId() const {
    return id;
}


// SweatyCustomer ------------------------
SweatyCustomer::SweatyCustomer(std::string name, int id):Customer(name,id){};
std::vector<int> SweatyCustomer::order(const std::vector<Workout> &workout_options){
    // filteredWorkouts only lives in this scope - therefore on stack;
    // option 2 initial in heap and delete before function close.
    std::vector<int> filteredWorkouts;
    for(int i = 0; i < workout_options.size(); ++i){
        if(workout_options[i].getType() == CARDIO){
            filteredWorkouts.push_back(workout_options[i].getId());
        }
    }
    return filteredWorkouts;
};

std::string SweatyCustomer::toString() const {
    return getName() + ",swt";
}

Customer* SweatyCustomer::clone() const{
    return new SweatyCustomer(getName(),getId());
}

//CheapCustomer --------------------------
//TODO: in the assignment it says this customer only orders once, maybe we should save a pointer/id of the cheapest workout elsewhere after calling.
CheapCustomer::CheapCustomer(std::string name, int id):Customer(name,id){};
std::vector<int> CheapCustomer::order(const std::vector<Workout> &workout_options){
    // we dereference cheapest to hold an alias to the first workout element in the list.
    const Workout *cheapest = &(workout_options[0]);
    for(int i = 1; i < workout_options.size(); ++i){
        if(workout_options[i].getPrice() < cheapest -> getPrice()){
            //changing the cheapest alias to another object.
            cheapest = &(workout_options[i]);
        }
    }
    return std::vector<int>{cheapest -> getId()};
};

std::string CheapCustomer::toString() const {
    return getName() + ",chp";
}

Customer* CheapCustomer::clone() const{
    return new CheapCustomer(getName(),getId());
}

//HeavyMuscleCustomer --------------------
HeavyMuscleCustomer::HeavyMuscleCustomer(std::string name, int id):Customer(name,id){};
std::vector<int> HeavyMuscleCustomer::order(const std::vector<Workout> &workout_options){
    std::vector<int> sortedWorkouts;
    //array of pointers to the filtered workouts.
    std::vector<const Workout*> filteredWorkouts;
    for(int i = 0; i <workout_options.size(); i++){
        if(workout_options[i].getType() == ANAEROBIC){
            filteredWorkouts.push_back(&(workout_options[i]));
        }
    }
    //sorting references by price (desc)
    sort( filteredWorkouts.begin( ), filteredWorkouts.end( ), [ ]( const Workout* lhs, const Workout* rhs )
    {
        return lhs -> getPrice() > rhs -> getPrice();
    });

//    replace references with workout ids
    for(int i = 0; i <filteredWorkouts.size(); i++){
        sortedWorkouts.push_back(filteredWorkouts[i]->getId());
    }
    return sortedWorkouts;
};

std::string HeavyMuscleCustomer::toString() const {
    return getName() + ",mcl";
}


Customer* HeavyMuscleCustomer::clone() const{
    return new HeavyMuscleCustomer(getName(),getId());
}

//FullBodyCustomer -----------------------
FullBodyCustomer::FullBodyCustomer(std::string name, int id):Customer(name,id){};
std::vector<int> FullBodyCustomer::order(const std::vector<Workout> &workout_options){
    // we dereference cheapest to hold an alias to the first workout element in the list.
    const Workout *cheapestCardio = nullptr;
    const Workout *expensiveMixed = nullptr;
    const Workout *cheapestAnaerobic = nullptr;
    std::vector<int> filteredWorkouts = std::vector<int>{};

    for(int i = 0; i < workout_options.size(); ++i) {
        if (workout_options[i].getType() == CARDIO) {
            if (cheapestCardio == nullptr) {
                cheapestCardio = &(workout_options[i]);
            } else if (workout_options[i].getPrice() < cheapestCardio->getPrice()) {
                cheapestCardio = &(workout_options[i]);
            }
        } else if (workout_options[i].getType() == MIXED) {
            if (expensiveMixed == nullptr) {
                expensiveMixed = &(workout_options[i]);
            } else if (workout_options[i].getPrice() > expensiveMixed->getPrice()) {
                expensiveMixed = &(workout_options[i]);
            }
        } else if (workout_options[i].getType() == ANAEROBIC) {
            if (cheapestAnaerobic == nullptr) {
                cheapestAnaerobic = &(workout_options[i]);
            } else if (workout_options[i].getPrice() < cheapestAnaerobic->getPrice()) {
                cheapestAnaerobic = &(workout_options[i]);
            }
        }
    }
//        assign the ids to a new vector
        if(cheapestCardio != nullptr)
            filteredWorkouts.push_back(cheapestCardio->getId());
        if(expensiveMixed != nullptr)
            filteredWorkouts.push_back(expensiveMixed->getId());
        if(cheapestAnaerobic != nullptr)
            filteredWorkouts.push_back(cheapestAnaerobic->getId());
//
        return filteredWorkouts;
};

std::string FullBodyCustomer::toString() const {
    return getName() + ",fbd";
}

Customer* FullBodyCustomer::clone() const{
    return new FullBodyCustomer(getName(),getId());
}
//
//int main(int argc, char** argv) {
//    FullBodyCustomer amit = FullBodyCustomer("amit", 4);
//    Workout running = Workout( 1, "running",  70, CARDIO);
//    Workout yoga = Workout( 2, "yoga",  20, MIXED);
//    Workout weights = Workout( 3, "weights",  25, ANAEROBIC);
//    Workout cycling = Workout( 4, "cycling",  1, MIXED);
//    Workout heat = Workout( 5, "heat",  25, CARDIO);
//    Workout crossfit = Workout( 6, "heat",  70, ANAEROBIC);
//    Workout boxing = Workout( 7, "boxing",  60, ANAEROBIC);
//
//    //this is a workouts pointer
//    std::vector<Workout> *workouts = new vector<Workout>{running,yoga,weights,cycling,heat,crossfit,boxing};
//
//    //passing the workouts object itself, the function gets it by reference.
//    std::vector<int> output = amit.order(*workouts);
//
//    for(auto &workout_id: output){
//        std::cout << workout_id << std::endl;
//    }
//}

